package com.tlongdev.bktf.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by ThanhLong on 2014.12.25..
 */
public class PriceListProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PriceListDbHelper mOpenHelper;

    public static final int PRICE_LIST = 100;
    public static final int PRICE_WITH_NAME = 101;
    public static final int PRICE_WITH_NAME_SPECIFIC = 102;
    public static final int PRICE_LIST_ID = 103;

    private static UriMatcher buildUriMatcher(){
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PriceListContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, PriceListContract.PATH_PRICE_LIST, PRICE_LIST);
        matcher.addURI(authority, PriceListContract.PATH_PRICE_LIST + "/name/*", PRICE_WITH_NAME);
        matcher.addURI(authority, PriceListContract.PATH_PRICE_LIST + "/name/*/*", PRICE_WITH_NAME_SPECIFIC);
        matcher.addURI(authority, PriceListContract.PATH_PRICE_LIST + "/id/#", PRICE_LIST_ID);

        return matcher;
    }

    private static final String sNameSelection =
            PriceListContract.PriceEntry.TABLE_NAME+
                    "." + PriceListContract.PriceEntry.COLUMN_ITEM_NAME + " = ? ";

    private static final String sNameSpecificSelection =
            PriceListContract.PriceEntry.TABLE_NAME +
                    "." + PriceListContract.PriceEntry.COLUMN_ITEM_NAME + " = ? AND " +
                    PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                    PriceListContract.PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                    PriceListContract.PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                    PriceListContract.PriceEntry.COLUMN_PRICE_INDEX + " = ?";

    @Override
    public boolean onCreate() {
        mOpenHelper = new PriceListDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "pricelist"
            case PRICE_LIST:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PriceListContract.PriceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "pricelist/*"
            case PRICE_WITH_NAME: {
                retCursor = getPricesByName(uri, projection, sortOrder);
                break;
            }
            // "pricelist/*/*"
            case PRICE_WITH_NAME_SPECIFIC: {
                retCursor = getSpecificPricesByName(uri, projection, sortOrder);
                break;
            }
            case PRICE_LIST_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PriceListContract.PriceEntry.TABLE_NAME,
                        projection,
                        PriceListContract.PriceEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRICE_LIST:
                return PriceListContract.PriceEntry.CONTENT_TYPE;
            case PRICE_WITH_NAME:
                return PriceListContract.PriceEntry.CONTENT_TYPE;
            case PRICE_WITH_NAME_SPECIFIC:
                return PriceListContract.PriceEntry.CONTENT_ITEM_TYPE;
            case PRICE_LIST_ID:
                return PriceListContract.PriceEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PRICE_LIST:
                long _id = db.insert(PriceListContract.PriceEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PriceListContract.PriceEntry.buildPriceListUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case PRICE_LIST:
                rowsDeleted = db.delete(
                        PriceListContract.PriceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case PRICE_LIST:
                rowsUpdated = db.update(PriceListContract.PriceEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRICE_LIST:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PriceListContract.PriceEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getSpecificPricesByName(Uri uri, String[] projection, String sortOrder) {
        String itemName = PriceListContract.PriceEntry.getNameFromUri(uri);
        String itemSpecification = PriceListContract.PriceEntry.getSpecificationFromUri(uri);

        String[] nameArray = {itemName};
        String[] selectionArgs = concatenate(nameArray, itemSpecification.split("-"));
        String selection = sNameSpecificSelection;

        return mOpenHelper.getReadableDatabase().query(
                PriceListContract.PriceEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getPricesByName(Uri uri, String[] projection, String sortOrder) {
        String itemName = PriceListContract.PriceEntry.getNameFromUri(uri);

        String[] selectionArgs = {itemName};
        String selection = sNameSelection;

        return mOpenHelper.getReadableDatabase().query(
                PriceListContract.PriceEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    public String[] concatenate(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c= new String[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}
