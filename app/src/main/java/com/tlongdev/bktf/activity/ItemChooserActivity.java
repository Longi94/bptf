package com.tlongdev.bktf.activity;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
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
import com.tlongdev.bktf.adapter.spinner.EffectAdapter;
import com.tlongdev.bktf.adapter.spinner.QualityAdapter;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Dialog style activity for selecting items to be added to the calculator list.
 */
public class ItemChooserActivity extends FragmentActivity {

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

    public static final String[] WEAPON_WEARS = {
            "Factory New", "Minimal Wear", "Field-Tested", "Well Worn", "Battle Scarred"
    };

    public static final int[] WEAPON_WEAR_IDS = {
            1045220557, 1053609165, 1058642330, 1061997773, 1065353216
    };

    public static final String[] EFFECT_COLUMNS = {
            UnusualSchemaEntry._ID,
            UnusualSchemaEntry.COLUMN_ID,
            UnusualSchemaEntry.COLUMN_NAME
    };

    public static final int COLUMN_INDEX = 1;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    @Bind(R.id.item_name) AutoCompleteTextView itemName;
    @Bind(R.id.quality) Spinner qualitySpinner;
    @Bind(R.id.effect) Spinner effectSpinner;
    @Bind(R.id.weapon_wear) Spinner wearSpinner;
    @Bind(R.id.title_effect) TextView titleEffect;
    @Bind(R.id.title_wear) TextView titleWear;

    private SimpleCursorAdapter nameAdapter;
    private Cursor effectCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_chooser);
        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //Set the color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Utility.getColor(this, R.color.primary_dark));
        }

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

        QualityAdapter qualityAdapter = new QualityAdapter(this,
                R.layout.quality_spinner_item);
        qualitySpinner.setAdapter(qualityAdapter);
        qualitySpinner.setSelection(7);
        qualitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                effectSpinner.setVisibility(View.GONE);
                wearSpinner.setVisibility(View.GONE);
                titleEffect.setVisibility(View.GONE);
                titleWear.setVisibility(View.GONE);
                switch (position) {
                    case 8:
                        effectSpinner.setVisibility(View.VISIBLE);
                        titleEffect.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        wearSpinner.setVisibility(View.VISIBLE);
                        titleWear.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //huh?
            }
        });

        effectCursor = getContentResolver().query(
                UnusualSchemaEntry.CONTENT_URI,
                EFFECT_COLUMNS,
                null,
                null,
                UnusualSchemaEntry.COLUMN_NAME + " ASC"
        );
        EffectAdapter effectAdapter = new EffectAdapter(this, R.layout.effect_spinner_item, effectCursor);
        effectSpinner.setAdapter(effectAdapter);

        ArrayAdapter<String> wearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, WEAPON_WEARS);
        wearSpinner.setAdapter(wearAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName(String.valueOf(getTitle()));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        effectCursor.close();
    }
}
