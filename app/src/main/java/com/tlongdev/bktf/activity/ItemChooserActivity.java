package com.tlongdev.bktf.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;

/**
 * Dialog style activity for selecting items to be added to the calculator list.
 */
public class ItemChooserActivity extends FragmentActivity{

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = ItemChooserActivity.class.getSimpleName();

    public static String[] COLUMNS = {
            ItemSchemaEntry._ID,
            ItemSchemaEntry.COLUMN_DEFINDEX,
            ItemSchemaEntry.COLUMN_ITEM_NAME
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_DEFINDEX = 1;
    public static final int COLUMN_NAME = 2;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    private AutoCompleteTextView itemName;

    private SimpleCursorAdapter nameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_chooser);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        itemName = (AutoCompleteTextView) findViewById(R.id.item_name);

        nameAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_dropdown_item_1line,
                null, new String[]{ItemSchemaEntry.COLUMN_ITEM_NAME}, new int[]{android.R.id.text1}, 0);
        nameAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(COLUMN_NAME);
            }
        });
        nameAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return getContentResolver().query(
                        ItemSchemaEntry.CONTENT_URI,
                        COLUMNS,
                        ItemSchemaEntry.COLUMN_ITEM_NAME + " LIKE ?",
                        new String[]{"%" + String.valueOf(constraint) + "%"},
                        ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC"
                );
            }
        });

        itemName.setAdapter(nameAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
