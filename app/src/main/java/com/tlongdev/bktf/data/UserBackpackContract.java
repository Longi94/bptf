package com.tlongdev.bktf.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class UserBackpackContract {

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
    public static final String PATH_BACKPACK = "backpack";

    public static final class UserBackpackEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BACKPACK).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BACKPACK;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BACKPACK;

        public static final String TABLE_NAME = "backpack";

        public static final String COLUMN_UNIQUE_ID = "unique_id";
        public static final String COLUMN_ORIGINAL_ID = "original_id";
        public static final String COLUMN_DEFINDEX = "defindex";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_ORIGIN = "origin";
        public static final String COLUMN_FLAG_CANNOT_TRADE = "flag_cannot_trade";
        public static final String COLUMN_FLAG_CANNOT_CRAFT = "flag_cannot_craft";
        public static final String COLUMN_INVENTORY_TOKEN = "inventory";
        public static final String COLUMN_QUALITY = "quality";
        public static final String COLUMN_CUSTOM_NAME = "custom_name";
        public static final String COLUMN_CUSTOM_DESCRIPTION = "custom_description";
        public static final String COLUMN_ATTRIBUTES = "attributes";
        public static final String COLUMN_EQUIPPED = "equipped";

        public static Uri buildBackPackUri(long id) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath("" + id).build();
        }
    }
}
