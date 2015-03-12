package com.tlongdev.bktf.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.tlongdev.bktf.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ItemSchemaDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "items.db";
    public static final int DATABASE_VERSION = 1;


    public static final String TABLE_NAME = "itemschema";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DEFINDEX = "defindex";
    public static final String COLUMN_TYPE = "type_name";
    public static final String COLUMN_PROPER_NAME = "proper_name";

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public ItemSchemaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        if (doesDbExist()) {
            openDatabase();
        } else {
            try {
                copyDatabase();
                openDatabase();
            } catch (IOException e) {
                if (Utility.isDebugging(mContext))
                    e.printStackTrace();
            }
        }
    }

    private boolean doesDbExist() {
        try {
            String path = mContext.getApplicationInfo().dataDir + "/databases/" + DATABASE_NAME;
            File file = new File(path);
            return file.exists();
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return false;
    }

    public void openDatabase() throws SQLException {
        //Open the database
        String path = mContext.getApplicationInfo().dataDir + "/databases/" + DATABASE_NAME;
        mDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    private void copyDatabase() throws IOException {
        //Open your local db as the input stream
        InputStream input = mContext.getAssets().open("databases/" + DATABASE_NAME);

        //Open the empty db as the output stream
        OutputStream output = new FileOutputStream(mContext.getApplicationInfo().dataDir + "/databases/" + DATABASE_NAME);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer))>0) {
            output.write(buffer, 0, length);
        }

        //Close the streams
        output.flush();
        output.close();
        input.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            copyDatabase();
        } catch (IOException e) {
            if (Utility.isDebugging(mContext))
                e.printStackTrace();
        }
    }

    public Cursor getItem(int defindex){
        if (mDatabase != null){
            Cursor item = mDatabase.query(
                    TABLE_NAME,
                    new String[]{COLUMN_NAME, COLUMN_TYPE, COLUMN_PROPER_NAME},
                    TABLE_NAME + "." + COLUMN_DEFINDEX + " = ?",
                    new String[]{"" + defindex},
                    null,
                    null,
                    null
            );
            if (item.moveToFirst()){
                return item;
            }
        }
        return null;
    }
}
