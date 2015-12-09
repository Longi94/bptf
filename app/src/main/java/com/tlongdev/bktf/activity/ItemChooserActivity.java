package com.tlongdev.bktf.activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
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

    public static String[] EFFECT_COLUMNS = {
            UnusualSchemaEntry._ID,
            UnusualSchemaEntry.COLUMN_ID,
            UnusualSchemaEntry.COLUMN_NAME
    };

    public static final int COLUMN_INDEX = 1;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    private AutoCompleteTextView itemName;

    private SimpleCursorAdapter nameAdapter;

    private Spinner qualitySpinner;
    private Spinner effectSpinner;

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
                R.layout.quality_spinner_item, QUALITIES);

        qualitySpinner.setAdapter(qualityAdapter);


        Cursor effectCursor = getContentResolver().query(
                UnusualSchemaEntry.CONTENT_URI,
                EFFECT_COLUMNS,
                null,
                null,
                UnusualSchemaEntry.COLUMN_NAME + " ASC"
        );
        EffectAdapter effectAdapter = new EffectAdapter(this, R.layout.effect_spinner_item, effectCursor);

        effectSpinner = (Spinner)findViewById(R.id.effect);
        effectSpinner.setAdapter(effectAdapter);
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
            View rootView = super.getView(position, convertView, parent);

            TextView text = (TextView)rootView.findViewById(R.id.text1);
            text.setText(getItem(position));

            quality.setQuality(QUALITY_IDS[position]);

            text.getCompoundDrawables()[0].setColorFilter(quality.getColor(ItemChooserActivity.this, false), PorterDuff.Mode.MULTIPLY);

            return rootView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView = super.getDropDownView(position, convertView, parent);

            TextView text = (TextView)rootView.findViewById(R.id.text1);
            text.setText(getItem(position));

            quality.setQuality(QUALITY_IDS[position]);

            text.getCompoundDrawables()[0].setColorFilter(quality.getColor(ItemChooserActivity.this, false), PorterDuff.Mode.MULTIPLY);

            return rootView;
        }
    }

    private class EffectAdapter extends SimpleCursorAdapter {

        private Item effect;

        public EffectAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c, new String[]{}, new int[]{}, 0);
            effect = new Item();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView = super.getView(position, convertView, parent);

            Cursor cursor = getCursor();
            if (cursor.moveToPosition(position)) {

                TextView text = (TextView) rootView.findViewById(R.id.text1);
                text.setText(cursor.getString(COLUMN_NAME));

                effect.setPriceIndex(cursor.getInt(COLUMN_INDEX));

                Glide.with(ItemChooserActivity.this)
                        .load(effect.getEffectUrl(ItemChooserActivity.this))
                        .into((ImageView) rootView.findViewById(R.id.effect));
            }

            return rootView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View rootView = super.getDropDownView(position, convertView, parent);

            Cursor cursor = getCursor();
            if (cursor.moveToPosition(position)) {

                TextView text = (TextView) rootView.findViewById(R.id.text1);
                text.setText(cursor.getString(COLUMN_NAME));

                effect.setPriceIndex(cursor.getInt(COLUMN_INDEX));

                Glide.with(ItemChooserActivity.this)
                        .load(effect.getEffectUrl(ItemChooserActivity.this))
                        .into((ImageView) rootView.findViewById(R.id.effect));
            }

            return rootView;
        }
    }
}
