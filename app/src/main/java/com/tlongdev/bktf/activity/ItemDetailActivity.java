package com.tlongdev.bktf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.UserBackpackContract.UserBackpackEntry;

import java.io.IOException;
import java.io.InputStream;

/**
 * The (dialog) activity for showing info about an item in a backpack.
 */
public class ItemDetailActivity extends Activity {

    public static final String LOG_TAG = ItemDetailActivity.class.getSimpleName();

    //Keys for the extra data in the intent
    public static final String EXTRA_ITEM_ID = "id";
    public static final String EXTRA_GUEST = "guest";
    public static final String EXTRA_ITEM_NAME = "name";
    public static final String EXTRA_ITEM_TYPE = "type";
    public static final String EXTRA_PROPER_NAME = "proper";

    //Indexes for the columns below
    public static final int COL_BACKPACK_DEFI = 1;
    public static final int COL_BACKPACK_QUAL = 2;
    public static final int COL_BACKPACK_CRFN = 3; // TODO
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
    public static final int COL_BACKPACK_EQUIP = 14; // TODO
    public static final int COL_BACKPACK_ORIGIN = 15;
    public static final int COL_BACKPACK_WEAR = 16;

    //Query columns for querying the price
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

    //Query columns for querying info of the item
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
            UserBackpackEntry.COLUMN_ORIGIN,
            UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR
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
            UserBackpackEntry.COLUMN_ORIGIN,
            UserBackpackEntry.COLUMN_DECORATED_WEAPON_WEAR
    };

    //This decides which table to load data from.
    private boolean isGuest;

    //This is the id of the item in the database table
    private int id;

    //Rerefernces to all the textviews in the view
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

    //References to the imageview
    private ImageView icon;
    private ImageView effectView;
    private ImageView paintView;

    //Store the intent that came
    private Intent mIntent;
    private CardView cardView;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        //Store the intent
        mIntent = getIntent();

        //Get extra data
        isGuest = mIntent.getBooleanExtra(EXTRA_GUEST, false);
        id = mIntent.getIntExtra(EXTRA_ITEM_ID, 0);

        //Scale the icon, so the width of the image view is on third of the screen's width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        FrameLayout layout = (FrameLayout) findViewById(R.id.relative_layout_image);
        layout.getLayoutParams().width = screenWidth / 3;
        layout.getLayoutParams().height = screenWidth / 3;
        layout.requestLayout();

        //Cardview which makes it look like a dialog
        cardView = (CardView) findViewById(R.id.card_view);

        //Return to the previous activity if the user taps utside te dialog.
        ((View) cardView.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 21)
                    finishAfterTransition();
                else
                    finish();
            }
        });
        //Do nathing if the user taps on the cardview itself
        cardView.setOnClickListener(null);

        //Find all the views
        name = (TextView) findViewById(R.id.text_view_name);
        level = (TextView) findViewById(R.id.text_view_level);
        effect = (TextView) findViewById(R.id.text_view_effect_name);
        customName = (TextView) findViewById(R.id.text_view_custom_name);
        customDesc = (TextView) findViewById(R.id.text_view_custom_desc);
        crafterName = (TextView) findViewById(R.id.text_view_crafted);
        gifterName = (TextView) findViewById(R.id.text_view_gifted);
        origin = (TextView) findViewById(R.id.text_view_origin);
        paint = (TextView) findViewById(R.id.text_view_paint);
        price = (TextView) findViewById(R.id.text_view_price);

        icon = (ImageView) findViewById(R.id.icon);
        effectView = (ImageView) findViewById(R.id.effect);
        paintView = (ImageView) findViewById(R.id.paint);

        queryItemDetails();
    }

    /**
     * Query all the necessary data out of the database and show them to de user.
     */
    private void queryItemDetails() {
        //Variables needed for querying
        Uri uri;
        String[] columns;
        String selection;

        if (isGuest) {
            //The user is a guest user
            uri = UserBackpackEntry.CONTENT_URI_GUEST;
            columns = QUERY_COLUMNS_GUEST;
            selection = UserBackpackEntry.TABLE_NAME_GUEST + "." +
                    UserBackpackEntry._ID + " = ?";
        } else {
            //The user is the main user
            uri = UserBackpackEntry.CONTENT_URI;
            columns = QUERY_COLUMNS;
            selection = UserBackpackEntry.TABLE_NAME + "." +
                    UserBackpackEntry._ID + " = ?";
        }

        //Query
        Cursor itemCursor = getContentResolver().query(
                uri,
                columns,
                selection,
                new String[]{String.valueOf(id)},
                null
        );

        int defindex;
        int priceIndex;
        int tradable;
        int craftable;
        int quality;
        int isAus;
        int wear;

        if (itemCursor.moveToFirst()) {
            //Store all the data
            defindex = itemCursor.getInt(COL_BACKPACK_DEFI);
            priceIndex = itemCursor.getInt(COL_BACKPACK_INDE);
            tradable = Math.abs(itemCursor.getInt(COL_BACKPACK_TRAD) - 1);
            craftable = Math.abs(itemCursor.getInt(COL_BACKPACK_CRAF) - 1);
            quality = itemCursor.getInt(COL_BACKPACK_QUAL);
            isAus = itemCursor.getInt(COL_BACKPACK_AUS);
            wear = itemCursor.getInt(COL_BACKPACK_WEAR);

            String customName = itemCursor.getString(COL_BACKPACK_CUSTOM_NAME);
            String customDescription = itemCursor.getString(COL_BACKPACK_CUSTOM_DESC);
            String crafter = itemCursor.getString(COL_BACKPACK_CRAFTER);
            String gifter = itemCursor.getString(COL_BACKPACK_GIFTER);

            int paintNumber = itemCursor.getInt(COL_BACKPACK_PAINT);

            //Set the name of the item
            name.setText(Utility.formatSimpleItemName(this,
                    defindex,
                    mIntent.getStringExtra(EXTRA_ITEM_NAME),
                    quality,
                    priceIndex,
                    mIntent.getIntExtra(EXTRA_PROPER_NAME, 0) == 1));

            //Set the level of the item, get the type from the intent
            if (defindex >= 15000 && defindex <= 15059) {
                level.setText(Utility.getDecoratedWeaponDesc(mIntent.getStringExtra(EXTRA_ITEM_TYPE), defindex, wear));
            } else {
                level.setText(getString(R.string.item_detail_level, itemCursor.getInt(COL_BACKPACK_LEVEL)
                        , mIntent.getStringExtra(EXTRA_ITEM_TYPE)));
            }

            //Set the origin of the item. Get the origin from the string array resource
            origin.setText(getString(R.string.item_detail_origin) + ": " +
                    getResources().getStringArray(R.array.array_origins)
                            [itemCursor.getInt(COL_BACKPACK_ORIGIN)]);

            //Set the effect of the item (if any)
            if (priceIndex != 0 && (quality == 5 || quality == 7 || quality == 9)) {
                effect.setText(getString(R.string.item_detail_effect) + ": " +
                        Utility.getUnusualEffectName(this, priceIndex));
                effect.setVisibility(View.VISIBLE);
            }

            //set the custom name of the item (if any)
            if (customName != null) {
                this.customName.setText(Html.fromHtml(getString(R.string.item_detail_custom_name) +
                        ": <i>" + customName + "</i>"));
                this.customName.setVisibility(View.VISIBLE);
            }

            //Set the custom description of the item (if any)
            if (customDescription != null) {
                customDesc.setText(Html.fromHtml(getString(R.string.item_detail_custom_description) +
                        ": <i>" + customDescription + "</i>"));
                customDesc.setVisibility(View.VISIBLE);
            }

            //Set the crafter's name (if any)
            if (crafter != null) {
                crafterName.setText(Html.fromHtml(getString(R.string.item_detail_craft) +
                        ": <i>" + crafter + "</i>"));
                crafterName.setVisibility(View.VISIBLE);
            }

            //Set the gifter's name (if any)
            if (gifter != null) {
                gifterName.setText(Html.fromHtml(getString(R.string.item_detail_gift) +
                        ": <i>" + gifter + "</i>"));
                gifterName.setVisibility(View.VISIBLE);
            }

            //Set the paint text (if any)
            if (paintNumber != 0) {
                paint.setText(getString(R.string.item_detail_paint) +
                        ": " + Utility.getPaintName(this, paintNumber));
                paint.setVisibility(View.VISIBLE);
            }

            //Set the icon and the background
            setIconImage(this, icon, effectView, paintView, Utility.getIconIndex(defindex), priceIndex,
                    quality, paintNumber, isAus == 1, wear);
            cardView.setCardBackgroundColor(Utility.getQualityColor(this, quality, defindex, true));
        } else {
            //Crash the app if there is no item with the id (should never happen)
            throw new RuntimeException("Item with id " + id + " not found (selection: "
                    + selection + ")");
        }

        //Start querying the price
        uri = PriceEntry.CONTENT_URI;
        columns = QUERY_COLUMNS_PRICE;

        //Proper condition for searching for australum items.
        String ausCondition;
        if (isAus == 1 || defindex == 5037) {
            ausCondition = PriceEntry.COLUMN_ITEM_NAME + " LIKE ?";
        } else {
            ausCondition = PriceEntry.COLUMN_ITEM_NAME + " NOT LIKE ?";
        }

        //Exact selection, should return only one match
        selection = PriceEntry.TABLE_NAME + "." +
                PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                ausCondition;

        //Query
        Cursor priceCursor = getContentResolver().query(
                uri,
                columns,
                selection,
                new String[]{String.valueOf(defindex), String.valueOf(quality),
                        String.valueOf(tradable), String.valueOf(craftable),
                        String.valueOf(priceIndex), "%australium%"},
                null
        );

        if (priceCursor.moveToFirst()) {
            //Show the price
            price.setVisibility(View.VISIBLE);
            price.setText(getString(R.string.item_detail_suggested_price) + ": " + Utility
                    .formatPrice(this, priceCursor.getDouble(COL_PRICE_LIST_PRICE),
                            priceCursor.getDouble(COL_PRICE_LIST_PMAX),
                            priceCursor.getString(COL_PRICE_LIST_CURRENCY),
                            priceCursor.getString(COL_PRICE_LIST_CURRENCY), false));
        }

        //Close the cursors
        itemCursor.close();
        priceCursor.close();
    }

    /**
     * Create and set the items icon according to it's properties
     *
     * @param context      context for accessing assets
     * @param icon         the ImageView to set the drawable to
     * @param defindex     defindex of the item
     * @param index        price index of the item
     * @param quality      quality of the item
     * @param paint        paint index of the item
     * @param isAustralium whether the item is australium or not
     */
    private void setIconImage(Context context, ImageView icon, ImageView effect, ImageView paint, int defindex, int index, int quality,
                              int paintColor, boolean isAustralium, int wear) {
        try {
            InputStream ims;
            AssetManager assetManager = context.getAssets();

            if (defindex >= 15000 && defindex <= 15059) {
                ims = assetManager.open("skins/" + Utility.getIconIndex(defindex) + "/" + wear + ".png");
            } else {
                if (isAustralium) {
                    ims = assetManager.open("items/" + defindex + "aus.png");
                } else {
                    ims = assetManager.open("items/" + defindex + ".png");
                }
            }
            //Load the item icon
            icon.setImageDrawable(Drawable.createFromStream(ims, null));

            if (index != 0 && Utility.canHaveEffects(defindex, quality)) {
                //Load the effect image
                ims = assetManager.open("effects/" + index + "_188x188.png");
                effect.setImageDrawable(Drawable.createFromStream(ims, null));
            }

            if (Utility.isPaint(paintColor)) {
                //Load the paint indicator dot
                ims = assetManager.open("paint/" + paintColor + ".png");
                paint.setImageDrawable(Drawable.createFromStream(ims, null));
            }

        } catch (IOException e) {
            if (Utility.isDebugging(context))
                e.printStackTrace();
        }
    }
}
