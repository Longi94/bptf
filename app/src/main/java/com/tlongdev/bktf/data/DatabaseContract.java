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
    public static final String PATH_PRICE_LIST = "pricelist";
    public static final String PATH_ITEM_SCHEMA = "schema";
    public static final String PATH_UNUSUAL_SCHEMA = "unusual_schema";
    public static final String PATH_ORIGIN_NAMES = "origin_names";
    public static final String PATH_DECORATED_WEAPONS = "decorated_weapons";
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_CALCULATOR = "calculator";
    public static final String PATH_BACKPACK = "backpack";


    /* Inner class that defines the table contents of the weather table */
    public static final class PriceEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRICE_LIST).build();

        public static final Uri ALL_PRICES_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PRICE_LIST)
                .appendPath("all")
                .build();

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
        public static final String COLUMN_IMAGE_LARGE = "image_large";
        public static final String COLUMN_IMAGE = "image";
    }

    public static final class UnusualSchemaEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_UNUSUAL_SCHEMA).build();

        public static final String TABLE_NAME = "unusual_schema";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
    }

    public static final class OriginEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ORIGIN_NAMES).build();

        public static final String TABLE_NAME = "origin_names";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
    }

    public static final class DecoratedWeaponEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DECORATED_WEAPONS).build();

        public static final String TABLE_NAME = "decorated_weapons";

        public static final String COLUMN_DEFINDEX = "defindex";
        public static final String COLUMN_GRADE = "grade";
    }
    public static final class FavoritesEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites";

        public static final String COLUMN_DEFINDEX = "defindex";
        public static final String COLUMN_ITEM_QUALITY = "quality";
        public static final String COLUMN_ITEM_TRADABLE = "tradable";
        public static final String COLUMN_ITEM_CRAFTABLE = "craftable";
        public static final String COLUMN_PRICE_INDEX = "price_index";
        public static final String COLUMN_AUSTRALIUM = "australium";
        public static final String COLUMN_WEAPON_WEAR = "weapon_wear";
    }

    public static final class CalculatorEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CALCULATOR).build();

        public static final String TABLE_NAME = "calculator";

        public static final String COLUMN_DEFINDEX = "defindex";
        public static final String COLUMN_ITEM_QUALITY = "quality";
        public static final String COLUMN_ITEM_TRADABLE = "tradable";
        public static final String COLUMN_ITEM_CRAFTABLE = "craftable";
        public static final String COLUMN_PRICE_INDEX = "price_index";
        public static final String COLUMN_AUSTRALIUM = "australium";
        public static final String COLUMN_WEAPON_WEAR = "weapon_wear";
        public static final String COLUMN_COUNT = "item_count";
    }
}
