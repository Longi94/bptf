package com.tlongdev.bktf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.data.PriceListContract.PriceEntry;
import com.tlongdev.bktf.data.UserBackpackContract.UserBackpackEntry;

import java.io.IOException;
import java.io.InputStream;


public class ItemDetailActivity extends Activity {

    public static final String EXTRA_ITEM_ID = "id";
    public static final String EXTRA_GUEST = "guest";
    public static final String EXTRA_ITEM_NAME = "name";
    public static final String EXTRA_ITEM_TYPE = "type";
    public static final String EXTRA_PROPER_NAME = "proper";

    //Query columns
    private static final String[] QUERY_COLUMNS = {
            UserBackpackEntry.TABLE_NAME + "." + UserBackpackEntry._ID,
            UserBackpackEntry.COLUMN_DEFINDEX,
            UserBackpackEntry.COLUMN_QUALITY,
            UserBackpackEntry.COLUMN_CRAFT_NUMBER,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
            UserBackpackEntry.COLUMN_ITEM_INDEX,
            UserBackpackEntry.COLUMN_PAINT,
            UserBackpackEntry.COLUMN_AUSTRALIUM,
            UserBackpackEntry.COLUMN_CREATOR_NAME,
            UserBackpackEntry.COLUMN_GIFTER_NAME,
            UserBackpackEntry.COLUMN_CUSTOM_NAME,
            UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION,
            UserBackpackEntry.COLUMN_LEVEL,
            UserBackpackEntry.COLUMN_EQUIPPED,
            UserBackpackEntry.COLUMN_ORIGIN
    };

    private static final String[] QUERY_COLUMNS_GUEST = {
            UserBackpackEntry.TABLE_NAME_GUEST + "." + UserBackpackEntry._ID,
            UserBackpackEntry.COLUMN_DEFINDEX,
            UserBackpackEntry.COLUMN_QUALITY,
            UserBackpackEntry.COLUMN_CRAFT_NUMBER,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_TRADE,
            UserBackpackEntry.COLUMN_FLAG_CANNOT_CRAFT,
            UserBackpackEntry.COLUMN_ITEM_INDEX,
            UserBackpackEntry.COLUMN_PAINT,
            UserBackpackEntry.COLUMN_AUSTRALIUM,
            UserBackpackEntry.COLUMN_CREATOR_NAME,
            UserBackpackEntry.COLUMN_GIFTER_NAME,
            UserBackpackEntry.COLUMN_CUSTOM_NAME,
            UserBackpackEntry.COLUMN_CUSTOM_DESCRIPTION,
            UserBackpackEntry.COLUMN_LEVEL,
            UserBackpackEntry.COLUMN_EQUIPPED,
            UserBackpackEntry.COLUMN_ORIGIN
    };

    //Indexes for the columns above
    public static final int COL_BACKPACK_DEFI = 1;
    public static final int COL_BACKPACK_QUAL = 2;
    public static final int COL_BACKPACK_CRFN = 3;
    public static final int COL_BACKPACK_TRAD = 4;
    public static final int COL_BACKPACK_CRAF = 5;
    public static final int COL_BACKPACK_INDE = 6;
    public static final int COL_BACKPACK_PAINT = 7;
    public static final int COL_BACKPACK_AUS = 8;
    public static final int COL_BACKPACK_CRAFTER = 9;
    public static final int COL_BACKPACK_GIFTER = 10;
    public static final int COL_BACKPACK_CUSTOM_NAME = 11;
    public static final int COL_BACKPACK_CUSTOM_DESC = 12;
    public static final int COL_BACKPACK_LEVEL = 13;
    public static final int COL_BACKPACK_EQUIP = 14;
    public static final int COL_BACKPACK_ORIGIN = 15;

    public static final String[] QUERY_COLUMNS_PRICE = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            PriceEntry.COLUMN_ITEM_PRICE,
            PriceEntry.COLUMN_ITEM_PRICE_MAX,
            PriceEntry.COLUMN_ITEM_PRICE_CURRENCY
    };

    //Indexes for the columns above
    public static final int COL_PRICE_LIST_PRICE = 1;
    public static final int COL_PRICE_LIST_PMAX = 2;
    public static final int COL_PRICE_LIST_CURRENCY = 3;

    private boolean isGuest;
    private int id;

    private TextView name;
    private TextView level;
    private TextView effect;
    private TextView customName;
    private TextView customDesc;
    private TextView crafterName;
    private TextView gifterName;
    private TextView origin;
    private TextView paint;
    private TextView price;

    private ImageView icon;
    private ImageView background;

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        mIntent = getIntent();

        isGuest = mIntent.getBooleanExtra(EXTRA_GUEST, false);
        id = mIntent.getIntExtra(EXTRA_ITEM_ID, 0);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        FrameLayout layout = (FrameLayout)findViewById(R.id.relative_layout_image);
        layout.getLayoutParams().width = screenWidth / 3;
        layout.getLayoutParams().height = screenWidth / 3;
        layout.requestLayout();

        final CardView cardView = (CardView)findViewById(R.id.card_view);

        ((View)cardView.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 21)
                    finishAfterTransition();
                else
                    finish();
            }
        });
        cardView.setOnClickListener(null);

        name = (TextView)findViewById(R.id.text_view_name);
        level = (TextView)findViewById(R.id.text_view_level);
        effect = (TextView)findViewById(R.id.text_view_effect_name);
        customName = (TextView)findViewById(R.id.text_view_custom_name);
        customDesc = (TextView)findViewById(R.id.text_view_custom_desc);
        crafterName = (TextView)findViewById(R.id.text_view_crafted);
        gifterName = (TextView)findViewById(R.id.text_view_gifted);
        origin = (TextView)findViewById(R.id.text_view_origin);
        paint = (TextView)findViewById(R.id.text_view_paint);
        price = (TextView)findViewById(R.id.text_view_price);

        icon = (ImageView)findViewById(R.id.image_view_item_icon);
        background = (ImageView)findViewById(R.id.image_view_item_background);

        queryItemDetails();
    }

    private void queryItemDetails() {
        Uri uri;
        String[] columns;
        String selection;
        if (isGuest) {
            uri = UserBackpackEntry.CONTENT_URI_GUEST;
            columns = QUERY_COLUMNS_GUEST;
            selection = UserBackpackEntry.TABLE_NAME_GUEST + "." +
                    UserBackpackEntry._ID + " = ?";
        } else {
            uri = UserBackpackEntry.CONTENT_URI;
            columns = QUERY_COLUMNS;
            selection = UserBackpackEntry.TABLE_NAME + "." +
                    UserBackpackEntry._ID + " = ?";
        }

        Cursor itemCursor = getContentResolver().query(
                uri,
                columns,
                selection,
                new String[]{"" + id},
                null
        );

        int defindex;
        int priceIndex;
        int tradable;
        int craftable;
        int quality;
        int isAus;
        if (itemCursor.moveToFirst()){
            defindex = itemCursor.getInt(COL_BACKPACK_DEFI);
            priceIndex = itemCursor.getInt(COL_BACKPACK_INDE);
            tradable = Math.abs(itemCursor.getInt(COL_BACKPACK_TRAD) - 1);
            craftable = Math.abs(itemCursor.getInt(COL_BACKPACK_CRAF) - 1);
            quality = itemCursor.getInt(COL_BACKPACK_QUAL);
            isAus = itemCursor.getInt(COL_BACKPACK_AUS);

            String customName = itemCursor.getString(COL_BACKPACK_CUSTOM_NAME);
            String customDescription = itemCursor.getString(COL_BACKPACK_CUSTOM_DESC);
            String crafter = itemCursor.getString(COL_BACKPACK_CRAFTER);
            String gifter = itemCursor.getString(COL_BACKPACK_GIFTER);
            int paintNumber = itemCursor.getInt(COL_BACKPACK_PAINT);

            name.setText(Utility.formatSimpleItemName(
                    mIntent.getStringExtra(EXTRA_ITEM_NAME),
                    quality,
                    priceIndex,
                    mIntent.getIntExtra(EXTRA_PROPER_NAME, 0) == 1));

            level.setText("Level " + itemCursor.getInt(COL_BACKPACK_LEVEL) + " " + mIntent.getStringExtra(EXTRA_ITEM_TYPE));

            origin.setText("Origin: " + getResources().getStringArray(R.array.origins)[itemCursor.getInt(COL_BACKPACK_ORIGIN)]);

            if (priceIndex != 0 && (quality == 5 || quality == 7 || quality == 9)){
                effect.setText("Effect: " + Utility.getUnusualEffectName(priceIndex));
                effect.setVisibility(View.VISIBLE);
            }

            if (customName != null){
                this.customName.setText(Html.fromHtml("Custom name: <i>" + customName + "</i>"));
                this.customName.setVisibility(View.VISIBLE);
            }

            if (customDescription != null){
                customDesc.setText(Html.fromHtml("Custom description: <i>" + customDescription + "</i>"));
                customDesc.setVisibility(View.VISIBLE);
            }

            if (crafter != null){
                crafterName.setText(Html.fromHtml("Crafted by: <i>" + crafter + "</i>"));
                crafterName.setVisibility(View.VISIBLE);
            }

            if (gifter != null){
                gifterName.setText(Html.fromHtml("Gifted by: <i>" + gifter + "</i>"));
                gifterName.setVisibility(View.VISIBLE);
            }

            if (paintNumber != 0){
                paint.setText("Paint: " + Utility.getPaintName(paintNumber));
                paint.setVisibility(View.VISIBLE);
            }

            setIconImage(this, icon, Utility.getIconIndex(defindex), priceIndex, quality, paintNumber, isAus == 1);
            background.setBackgroundDrawable(Utility.getItemBackground(this,
                    quality, tradable, craftable));
        } else {
            throw new RuntimeException("Item with id " + id + " not found (selection: " + selection + ")");
        }

        uri = PriceEntry.CONTENT_URI;
        columns = QUERY_COLUMNS_PRICE;
        String ausCondition;
        if (isAus == 1 || defindex == 5037) {
            ausCondition = PriceEntry.COLUMN_ITEM_NAME + " LIKE ?";
        } else {
            ausCondition = PriceEntry.COLUMN_ITEM_NAME + " NOT LIKE ?";
        }

        selection = PriceEntry.TABLE_NAME + "." +
                PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                ausCondition;

        Cursor priceCursor = getContentResolver().query(
                uri,
                columns,
                selection,
                new String[]{"" + defindex, "" + quality, "" + tradable, "" + craftable, "" + priceIndex, "%australium%"},
                null
        );

        if (priceCursor.moveToFirst()) {
            try {
                price.setVisibility(View.VISIBLE);
                price.setText("Suggested price: " + Utility.formatPrice(this, priceCursor.getDouble(COL_PRICE_LIST_PRICE),
                        priceCursor.getDouble(COL_PRICE_LIST_PMAX), priceCursor.getString(COL_PRICE_LIST_CURRENCY),
                        priceCursor.getString(COL_PRICE_LIST_CURRENCY), false));
            } catch (Throwable throwable) {
                if (Utility.isDebugging(this))
                    throwable.printStackTrace();
            }
        }

        itemCursor.close();
        priceCursor.close();
    }

    private void setIconImage(Context context, ImageView icon, int defindex, int index, int quality, int paint, boolean isAustralium) {
        try {
            InputStream ims;
            AssetManager assetManager = context.getAssets();
            Drawable d;

            if (isAustralium) {
                ims = assetManager.open("items/" + defindex + "aus.png");
            } else {
                ims = assetManager.open("items/" + defindex + ".png");
            }

            Drawable iconDrawable = Drawable.createFromStream(ims, null);
            if (index != 0 && Utility.canHaveEffects(defindex, quality)) {
                ims = assetManager.open("effects/" + index + "_188x188.png");
                Drawable effectDrawable = Drawable.createFromStream(ims, null);
                d = new LayerDrawable(new Drawable[]{effectDrawable, iconDrawable});
            } else {
                d = iconDrawable;
            }

            if (Utility.isPaint(paint)){
                ims = assetManager.open("paint/" + paint + ".png");
                Drawable paintDrawable = Drawable.createFromStream(ims, null);
                d = new LayerDrawable(new Drawable[]{d, paintDrawable});
            }
            // set image to ImageView
            icon.setImageDrawable(d);
        } catch (IOException e) {
            Toast.makeText(context, "bptf: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (Utility.isDebugging(context))
                e.printStackTrace();
        }
    }
}
