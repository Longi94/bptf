package com.tlongdev.bktf.data;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the database.
 */
public final class PriceListContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.tlongdev.bktf";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_PRICE_LIST = "pricelist";

    /* Inner class that defines the table contents of the weather table */
    public static final class PriceEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRICE_LIST).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PRICE_LIST;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PRICE_LIST;

        public static final String TABLE_NAME = "pricelist";

        public static final String COLUMN_DEFINDEX = "defindex";
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_QUALITY = "quality";
        public static final String COLUMN_ITEM_TRADABLE = "tradable";
        public static final String COLUMN_ITEM_CRAFTABLE = "craftable";
        public static final String COLUMN_ITEM_PRICE_CURRENCY = "currency";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_PRICE_MAX = "max";
        public static final String COLUMN_ITEM_PRICE_RAW = "raw";
        public static final String COLUMN_LAST_UPDATE = "last_update";
        public static final String COLUMN_DIFFERENCE = "difference";
        public static final String COLUMN_PRICE_INDEX = "price_index";

        public static Uri buildPriceListUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath("" + id).build();
        }

        public static Uri buildPriceListUriWithName(String name){
            return CONTENT_URI.buildUpon().appendPath("name").appendPath(name).build();
        }

        public static Uri buildPriceListUriWithNameSpecific(String name, int quality, int tradable, int craftable, int index){
            return CONTENT_URI.buildUpon().appendPath("name").appendPath(name)
                    .appendPath("" + quality + "-" + tradable + "-" + craftable + "-" + index).build();
        }

        public static Uri buildPriceListSearchUri(String name){
            return CONTENT_URI.buildUpon().appendPath(SearchManager.SUGGEST_URI_PATH_QUERY).appendPath(name).build();
        }

        public static String getNameFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getSpecificationFromUri(Uri uri){
            return uri.getPathSegments().get(3);
        }
    }
}
