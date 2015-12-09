package com.tlongdev.bktf.activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

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

    public static final int COLUMN_DEFINDEX = 1;
    public static final int COLUMN_NAME = 2;

    public static final String[] QUALITIES = {
            "Collector's", "Decorated Weapon", "Genuine", "Haunted", "Normal", "Self-Made",
            "Strange", "Unique", "Unusual", "Vintage"
    };
    public static final int[] QUALITY_IDS = {
            Quality.COLLECTORS, Quality.PAINTKITWEAPON, Quality.GENUINE, Quality.HAUNTED, Quality.NORMAL,
            Quality.SELF_MADE, Quality.STRANGE, Quality.UNIQUE, Quality.UNUSUAL, Quality.VINTAGE
    };

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    private AutoCompleteTextView itemName;

    private SimpleCursorAdapter nameAdapter;

    private Spinner qualitySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_chooser);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

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

        qualitySpinner = (Spinner) findViewById(R.id.quality);
        QualityAdapter qualityAdapter = new QualityAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, QUALITIES);

        qualitySpinner.setAdapter(qualityAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private class QualityAdapter extends ArrayAdapter<String> {

        private Item quality;

        public QualityAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
            quality = new Item();
            quality.setDefindex(15059);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getQualityView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getQualityView(position, convertView, parent);
        }

        private View getQualityView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();

            View view = inflater.inflate(R.layout.quality_spinner_item, parent, false);

            TextView text = (TextView)view.findViewById(R.id.text1);
            text.setText(getItem(position));

            quality.setQuality(QUALITY_IDS[position]);

            text.getCompoundDrawables()[0].setColorFilter(quality.getColor(ItemChooserActivity.this, false), PorterDuff.Mode.MULTIPLY);

            return view;
        }
    }
}
