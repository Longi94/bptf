package com.tlongdev.bktf.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.tlongdev.bktf.data.DatabaseContract.*;
import com.tlongdev.bktf.util.Utility;

public class DatabaseProvider extends ContentProvider {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = DatabaseProvider.class.getSimpleName();

    /**
     * URI matcher result codes
     */
    private static final int PRICE_LIST = 100;
    private static final int ITEM_SCHEMA = 101;
    private static final int ORIGIN_NAMES = 102;
    private static final int UNUSUAL_SCHEMA = 103;
    private static final int BACKPACK = 104;
    private static final int BACKPACK_GUEST = 105;
    private static final int FAVORITES = 106;
    private static final int CALCULATOR = 107;
    private static final int DECORATED_WEAPON = 108;
    private static final int ALL_PRICES = 109;

    /**
     * The URI Matcher used by this content provider
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /**
     * The database helper.
     */
    private DatabaseHelper mOpenHelper;

    /**
     * Builds an URI matcher to match the given URIs
     *
     * @return the URI matcher
     */
    private static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DatabaseContract.PATH_PRICE_LIST, PRICE_LIST);
        matcher.addURI(authority, DatabaseContract.PATH_ITEM_SCHEMA, ITEM_SCHEMA);
        matcher.addURI(authority, DatabaseContract.PATH_UNUSUAL_SCHEMA, UNUSUAL_SCHEMA);
        matcher.addURI(authority, DatabaseContract.PATH_ORIGIN_NAMES, ORIGIN_NAMES);
        matcher.addURI(authority, DatabaseContract.PATH_DECORATED_WEAPONS, DECORATED_WEAPON);
        matcher.addURI(authority, DatabaseContract.PATH_BACKPACK, BACKPACK);
        matcher.addURI(authority, DatabaseContract.PATH_FAVORITES, FAVORITES);
        matcher.addURI(authority, DatabaseContract.PATH_CALCULATOR, CALCULATOR);
        matcher.addURI(authority, DatabaseContract.PATH_BACKPACK + "/guest", BACKPACK_GUEST);
        matcher.addURI(authority, DatabaseContract.PATH_PRICE_LIST + "/all", ALL_PRICES);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                tableName = PriceEntry.TABLE_NAME;
                break;
            case ITEM_SCHEMA:
                tableName = ItemSchemaEntry.TABLE_NAME;
                break;
            case UNUSUAL_SCHEMA:
                tableName = UnusualSchemaEntry.TABLE_NAME;
                break;
            case ORIGIN_NAMES:
                tableName = OriginEntry.TABLE_NAME;
                break;
            case BACKPACK:
                tableName = UserBackpackEntry.TABLE_NAME;
                break;
            case BACKPACK_GUEST:
                tableName = UserBackpackEntry.TABLE_NAME_GUEST;
                break;
            case FAVORITES:
                tableName = FavoritesEntry.TABLE_NAME;
                break;
            case CALCULATOR:
                tableName = CalculatorEntry.TABLE_NAME;
                break;
            case DECORATED_WEAPON:
                tableName = DecoratedWeaponEntry.TABLE_NAME;
                break;
            case ALL_PRICES:
                retCursor = queryAllPrices();
                if (getContext() != null && retCursor != null) {
                    retCursor.setNotificationUri(getContext().getContentResolver(), uri);
                }
                return retCursor;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor = mOpenHelper.getReadableDatabase().query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (getContext() != null && retCursor != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return retCursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_PRICE_LIST;
            case ITEM_SCHEMA:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_ITEM_SCHEMA;
            case UNUSUAL_SCHEMA:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_UNUSUAL_SCHEMA;
            case ORIGIN_NAMES:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_ORIGIN_NAMES;
            case BACKPACK:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_BACKPACK;
            case BACKPACK_GUEST:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_BACKPACK;
            case FAVORITES:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_FAVORITES;
            case CALCULATOR:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_CALCULATOR;
            case DECORATED_WEAPON:
                return "vnd.android.cursor.dir/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_DECORATED_WEAPONS;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long _id;
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                tableName = PriceEntry.TABLE_NAME;
                returnUri = PriceEntry.CONTENT_URI;
                break;
            case ITEM_SCHEMA:
                tableName = ItemSchemaEntry.TABLE_NAME;
                returnUri = ItemSchemaEntry.CONTENT_URI;
                break;
            case UNUSUAL_SCHEMA:
                tableName = UnusualSchemaEntry.TABLE_NAME;
                returnUri = UnusualSchemaEntry.CONTENT_URI;
                break;
            case ORIGIN_NAMES:
                tableName = OriginEntry.TABLE_NAME;
                returnUri = OriginEntry.CONTENT_URI;
                break;
            case BACKPACK:
                tableName = UserBackpackEntry.TABLE_NAME;
                returnUri = UserBackpackEntry.CONTENT_URI;
                break;
            case BACKPACK_GUEST:
                tableName = UserBackpackEntry.TABLE_NAME_GUEST;
                returnUri = UserBackpackEntry.CONTENT_URI_GUEST;
                break;
            case FAVORITES:
                tableName = FavoritesEntry.TABLE_NAME;
                returnUri = FavoritesEntry.CONTENT_URI;
                break;
            case CALCULATOR:
                tableName = CalculatorEntry.TABLE_NAME;
                returnUri = CalculatorEntry.CONTENT_URI;
                break;
            case DECORATED_WEAPON:
                tableName = DecoratedWeaponEntry.TABLE_NAME;
                returnUri = DecoratedWeaponEntry.CONTENT_URI;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        _id = db.insert(tableName, null, values);
        if (_id > 0)
            return returnUri;
        else
            throw new android.database.SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(uri)) {

            case PRICE_LIST:
                rowsDeleted = db.delete(PriceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_SCHEMA:
                rowsDeleted = db.delete(ItemSchemaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case UNUSUAL_SCHEMA:
                rowsDeleted = db.delete(UnusualSchemaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ORIGIN_NAMES:
                rowsDeleted = db.delete(OriginEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BACKPACK:
                rowsDeleted = db.delete(UserBackpackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BACKPACK_GUEST:
                rowsDeleted = db.delete(UserBackpackEntry.TABLE_NAME_GUEST, selection, selectionArgs);
                break;
            case FAVORITES:
                rowsDeleted = db.delete(FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CALCULATOR:
                rowsDeleted = db.delete(CalculatorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DECORATED_WEAPON:
                rowsDeleted = db.delete(DecoratedWeaponEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (getContext() != null && (selection == null || rowsDeleted != 0)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                rowsUpdated = db.update(PriceEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ITEM_SCHEMA:
                rowsUpdated = db.update(ItemSchemaEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case UNUSUAL_SCHEMA:
                rowsUpdated = db.update(UnusualSchemaEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ORIGIN_NAMES:
                rowsUpdated = db.update(OriginEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BACKPACK:
                rowsUpdated = db.update(UserBackpackEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BACKPACK_GUEST:
                rowsUpdated = db.update(UserBackpackEntry.TABLE_NAME_GUEST, values, selection, selectionArgs);
                break;
            case FAVORITES:
                rowsUpdated = db.update(FavoritesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CALCULATOR:
                rowsUpdated = db.update(CalculatorEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case DECORATED_WEAPON:
                rowsUpdated = db.update(DecoratedWeaponEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null && rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case PRICE_LIST:
                tableName = PriceEntry.TABLE_NAME;
                break;
            case ITEM_SCHEMA:
                tableName = ItemSchemaEntry.TABLE_NAME;
                break;
            case UNUSUAL_SCHEMA:
                tableName = UnusualSchemaEntry.TABLE_NAME;
                break;
            case ORIGIN_NAMES:
                tableName = OriginEntry.TABLE_NAME;
                break;
            case BACKPACK:
                tableName = UserBackpackEntry.TABLE_NAME;
                break;
            case BACKPACK_GUEST:
                tableName = UserBackpackEntry.TABLE_NAME_GUEST;
                break;
            case FAVORITES:
                tableName = FavoritesEntry.TABLE_NAME;
                break;
            case CALCULATOR:
                tableName = CalculatorEntry.TABLE_NAME;
                break;
            case DECORATED_WEAPON:
                tableName = DecoratedWeaponEntry.TABLE_NAME;
                break;
            default:
                return super.bulkInsert(uri, values);
        }

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnCount;
    }

    private Cursor queryAllPrices() {
        Cursor cursor = mOpenHelper.getReadableDatabase().rawQuery("SELECT " +
                        PriceEntry.TABLE_NAME + "." + PriceEntry._ID + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + "," +
                        ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                        Utility.getRawPriceQueryString(getContext()) + " raw_price," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DIFFERENCE + "," +
                        PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                        " FROM " + PriceEntry.TABLE_NAME +
                        " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                        " ON " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                        " ORDER BY " + PriceEntry.COLUMN_LAST_UPDATE + " DESC",
                null
        );

        //Raw query is lazy, it won't actually query until we actually ask for the data.
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                return cursor;
            }
        }

        return cursor;
    }
}
