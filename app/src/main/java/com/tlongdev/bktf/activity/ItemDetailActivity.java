package com.tlongdev.bktf.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.squareup.picasso.Picasso;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.data.DatabaseHelper;
import com.tlongdev.bktf.data.UserBackpackContract.UserBackpackEntry;
import com.tlongdev.bktf.model.BackpackItem;
import com.tlongdev.bktf.model.Price;

import java.io.IOException;
import java.io.InputStream;

/**
 * The (dialog) activity for showing info about an item in a backpack.
 */
public class ItemDetailActivity extends Activity {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = ItemDetailActivity.class.getSimpleName();

    //Keys for the extra data in the intent
    public static final String EXTRA_ITEM_ID = "id";
    public static final String EXTRA_GUEST = "guest";
    public static final String EXTRA_ITEM_NAME = "name";
    public static final String EXTRA_ITEM_TYPE = "type";
    public static final String EXTRA_PROPER_NAME = "proper";

    //Indexes for the columns below
    public static final int COLUMN_DEFINDEX = 1;
    public static final int COLUMN_QUALITY = 2;
    // TODO public static final int COLUMN_CRAFT_NUMBER = 3;
    public static final int COLUMN_TRADABLE = 4;
    public static final int COLUMN_CRAFTABLE = 5;
    public static final int COLUMN_PRICE_INDEX = 6;
    public static final int COLUMN_PAINT = 7;
    public static final int COLUMN_AUSTRALIUM = 8;
    public static final int COLUMN_CRAFTER = 9;
    public static final int COLUMN_GIFTER = 10;
    public static final int COLUMN_CUSTOM_NAME = 11;
    public static final int COLUMN_CUSTOM_DESCRIPTION = 12;
    public static final int COLUMN_LEVEL = 13;
    // TODO public static final int COLUMN_EQUIPPED = 14;
    public static final int COLUMN_ORIGIN = 15;
    public static final int COLUMN_WEAPON_WEAR = 16;

    //Indexes for the columns above
    public static final int COLUMN_PRICE = 0;
    public static final int COLUMN_PRICE_HIGH = 1;
    public static final int COLUMN_CURRENCY = 2;

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

    //References to all the text views in the view
    private TextView name;
    private TextView level;
    private TextView effect;
    private TextView customName;
    private TextView customDesc;
    private TextView crafterName;
    private TextView gifterName;
    private TextView origin;
    private TextView paint;
    private TextView priceView;

    //References to the image view
    private ImageView icon;
    private ImageView effectView;
    private ImageView paintView;

    //Store the intent that came
    private Intent mIntent;
    private CardView cardView;

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

        //Card view which makes it look like a dialog
        cardView = (CardView) findViewById(R.id.card_view);

        //Return to the previous activity if the user taps outside te dialog.
        ((View) cardView.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 21)
                    finishAfterTransition();
                else
                    finish();
            }
        });
        //Do nothing if the user taps on the card view itself
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
        priceView = (TextView) findViewById(R.id.text_view_price);

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

        if (itemCursor != null) {
            if (itemCursor.moveToFirst()) {
                //Store all the data
                BackpackItem item = new BackpackItem(
                        itemCursor.getInt(COLUMN_DEFINDEX),
                        mIntent.getStringExtra(EXTRA_ITEM_NAME),
                        itemCursor.getInt(COLUMN_QUALITY),
                        Math.abs(itemCursor.getInt(COLUMN_TRADABLE) - 1) == 1,
                        Math.abs(itemCursor.getInt(COLUMN_CRAFTABLE) - 1) == 1,
                        itemCursor.getInt(COLUMN_AUSTRALIUM) == 1,
                        itemCursor.getInt(COLUMN_PRICE_INDEX),
                        itemCursor.getInt(COLUMN_WEAPON_WEAR),
                        null,
                        0,
                        0,
                        itemCursor.getInt(COLUMN_LEVEL),
                        itemCursor.getInt(COLUMN_ORIGIN),
                        itemCursor.getInt(COLUMN_PAINT),
                        0,
                        itemCursor.getString(COLUMN_CUSTOM_NAME),
                        itemCursor.getString(COLUMN_CUSTOM_DESCRIPTION),
                        itemCursor.getString(COLUMN_CRAFTER),
                        itemCursor.getString(COLUMN_GIFTER),
                        null,
                        false
                );

                //Set the name of the item
                name.setText(item.getFormattedName(this, mIntent.getIntExtra(EXTRA_PROPER_NAME, 0) == 1));

                //Set the level of the item, get the type from the intent
                if (item.getDefindex() >= 15000 && item.getDefindex() <= 15059) {
                    level.setText(item.getDecoratedWeaponDesc(mIntent.getStringExtra(EXTRA_ITEM_TYPE)));
                } else {
                    level.setText(getString(R.string.item_detail_level,
                            item.getLevel(),
                            mIntent.getStringExtra(EXTRA_ITEM_TYPE)));
                }

                //Set the origin of the item. Get the origin from the string array resource
                origin.setText(String.format("%s: %s", getString(R.string.item_detail_origin), Utility.getOriginName(this, item.getOrigin())));

                //Set the effect of the item (if any)
                if (item.getPriceIndex() != 0 && (item.getQuality() == 5 || item.getQuality() == 7 || item.getQuality() == 9)) {
                    effect.setText(String.format("%s: %s", getString(R.string.item_detail_effect),
                            Utility.getUnusualEffectName(this, item.getPriceIndex())));
                    effect.setVisibility(View.VISIBLE);
                }

                //set the custom name of the item (if any)
                if (item.getCustomName() != null) {
                    customName.setText(Html.fromHtml(String.format("%s: <i>%s</i>",
                            getString(R.string.item_detail_custom_name), item.getCustomName())));
                    customName.setVisibility(View.VISIBLE);
                }

                //Set the custom description of the item (if any)
                if (item.getCustomDescription() != null) {
                    customDesc.setText(Html.fromHtml(String.format("%s: <i>%s</i>",
                            getString(R.string.item_detail_custom_description), item.getCustomDescription())));
                    customDesc.setVisibility(View.VISIBLE);
                }

                //Set the crafter's name (if any)
                if (item.getCreatorName() != null) {
                    crafterName.setText(Html.fromHtml(String.format("%s: <i>%s</i>",
                            getString(R.string.item_detail_craft), item.getCreatorName())));
                    crafterName.setVisibility(View.VISIBLE);
                }

                //Set the gifter's name (if any)
                if (item.getGifterName() != null) {
                    gifterName.setText(Html.fromHtml(String.format("%s: <i>%s</i>",
                            getString(R.string.item_detail_gift), item.getGifterName())));
                    gifterName.setVisibility(View.VISIBLE);
                }

                //Set the paint text (if any)
                if (item.getPaint() != 0) {
                    paint.setText(String.format("%s: %s", getString(R.string.item_detail_paint), item.getPaintName(this)));
                    paint.setVisibility(View.VISIBLE);
                }

                //Set the icon and the background
                Picasso picasso = Picasso.with(this);
                picasso.setLoggingEnabled(true);
                picasso.setIndicatorsEnabled(true);
                picasso.load(item.getIconUrl(this)).into(icon);

                try {
                    effectView.setImageDrawable(item.getEffectDrawable(this));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    InputStream ims;
                    AssetManager assetManager = getAssets();
                    if (BackpackItem.isPaint(item.getPaint())) {
                        //Load the paint indicator dot
                        ims = assetManager.open("paint/" + item.getPaint() + ".png");
                        paintView.setImageDrawable(Drawable.createFromStream(ims, null));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                cardView.setCardBackgroundColor(item.getColor(this, true));

                //Start querying the priceView

                String sql = "SELECT " +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY +
                        " FROM " + PriceEntry.TABLE_NAME +
                        " WHERE " + PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                        PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                        PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                        PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                        PriceEntry.COLUMN_AUSTRALIUM + " = ?";

                DatabaseHelper dbHelper = new DatabaseHelper(this);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                Cursor priceCursor = db.rawQuery(sql, new String[]{String.valueOf(item.getDefindex()), String.valueOf(item.getQuality()),
                        String.valueOf(item.isTradable() ? 1 : 0), String.valueOf(item.isCraftable() ? 1 : 0),
                        String.valueOf(item.getPriceIndex()), String.valueOf(item.isAustralium() ? 1 : 0)});

                if (priceCursor != null) {
                    if (priceCursor.moveToFirst()) {
                        Price price = new Price(
                                priceCursor.getDouble(COLUMN_PRICE),
                                priceCursor.getDouble(COLUMN_PRICE_HIGH),
                                0, 0, 0,
                                priceCursor.getString(COLUMN_CURRENCY)
                        );
                        //Show the priceView
                        priceView.setVisibility(View.VISIBLE);
                        priceView.setText(String.format("%s: %s",
                                getString(R.string.item_detail_suggested_price),
                                price.getFormattedPrice(this)));
                    }
                    priceCursor.close();
                }
            }
            itemCursor.close();
        } else {
            //Crash the app if there is no item with the id (should never happen)
            throw new RuntimeException("Item with id " + id + " not found (selection: "
                    + selection + ")");
        }
    }
}
