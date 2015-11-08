package com.tlongdev.bktf.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the database.
 */
public final class DatabaseContract {

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
    public static final String PATH_RAW_QUERY = "raw";
    public static final String PATH_PRICE_LIST = "pricelist";
    public static final String PATH_ITEM_SCHEMA = "schema";
    public static final String PATH_UNUSUAL_SCHEMA = "unusual_schema";
    public static final String PATH_ORIGIN_NAMES = "origin_names";
    public static final String PATH_BACKPACK = "backpack";

    public static final Uri RAW_QUERY_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RAW_QUERY).build();

    /* Inner class that defines the table contents of the weather table */
    public static final class PriceEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRICE_LIST).build();

        public static final String TABLE_NAME = "pricelist";

        public static final String COLUMN_DEFINDEX = "defindex";
        public static final String COLUMN_ITEM_QUALITY = "quality";
        public static final String COLUMN_ITEM_TRADABLE = "tradable";
        public static final String COLUMN_ITEM_CRAFTABLE = "craftable";
        public static final String COLUMN_PRICE_INDEX = "price_index";
        public static final String COLUMN_AUSTRALIUM = "australium";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_PRICE_HIGH = "max";
        public static final String COLUMN_CURRENCY = "currency";
        public static final String COLUMN_LAST_UPDATE = "last_update";
        public static final String COLUMN_DIFFERENCE = "difference";
        public static final String COLUMN_WEAPON_WEAR = "weapon_wear";

        public static Uri buildUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath("" + id).build();
        }
    }

    /* Inner class that defines the table contents of the weather table */
    public static final class ItemSchemaEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM_SCHEMA).build();

        public static final String TABLE_NAME = "item_schema";

        public static final String COLUMN_DEFINDEX = "defindex";
        public static final String COLUMN_ITEM_NAME = "item_name";
        public static final String COLUMN_TYPE_NAME = "type_name";
        public static final String COLUMN_PROPER_NAME = "proper_name";
        public static final String COLUMN_DESCRIPTION = "description";

        public static Uri buildUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath("" + id).build();
        }
    }

    public static final class UnusualSchemaEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_UNUSUAL_SCHEMA).build();

        public static final String TABLE_NAME = "unusual_schema";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";

        public static Uri buildUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath("" + id).build();
        }
    }

    public static final class OriginEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ORIGIN_NAMES).build();

        public static final String TABLE_NAME = "origin_names";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";

        public static Uri buildUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath("" + id).build();
        }
    }

    public static final class UserBackpackEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BACKPACK).build();

        public static final Uri CONTENT_URI_GUEST =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BACKPACK).appendPath("guest").build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BACKPACK;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BACKPACK;

        public static final String TABLE_NAME = "backpack";
        public static final String TABLE_NAME_GUEST = "backpack_guest";

        public static final String COLUMN_POSITION = "position";
        public static final String COLUMN_UNIQUE_ID = "unique_id";
        public static final String COLUMN_ORIGINAL_ID = "original_id";
        public static final String COLUMN_DEFINDEX = "defindex";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_ORIGIN = "origin";
        public static final String COLUMN_FLAG_CANNOT_TRADE = "flag_cannot_trade";
        public static final String COLUMN_FLAG_CANNOT_CRAFT = "flag_cannot_craft";
        public static final String COLUMN_QUALITY = "quality";
        public static final String COLUMN_CUSTOM_NAME = "custom_name";
        public static final String COLUMN_CUSTOM_DESCRIPTION = "custom_description";
        public static final String COLUMN_EQUIPPED = "equipped";
        public static final String COLUMN_ITEM_INDEX = "item_index";
        public static final String COLUMN_PAINT = "paint";
        public static final String COLUMN_CRAFT_NUMBER = "craft_index";
        public static final String COLUMN_CREATOR_NAME = "creator_name";
        public static final String COLUMN_GIFTER_NAME = "gifter_name";
        public static final String COLUMN_CONTAINED_ITEM = "contained_item";
        public static final String COLUMN_AUSTRALIUM = "australium";
        public static final String COLUMN_DECORATED_WEAPON_WEAR = "weapon_wear";

        public static Uri buildUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath("" + id).build();
        }
    }
}
