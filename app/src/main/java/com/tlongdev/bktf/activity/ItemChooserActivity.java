package com.tlongdev.bktf.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.spinner.EffectAdapter;
import com.tlongdev.bktf.adapter.spinner.QualityAdapter;
import com.tlongdev.bktf.adapter.spinner.WeaponWearAdapter;
import com.tlongdev.bktf.data.DatabaseContract.CalculatorEntry;
import com.tlongdev.bktf.data.DatabaseContract.UnusualSchemaEntry;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Quality;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog style activity for selecting items to be added to the calculator list.
 */
public class ItemChooserActivity extends AppCompatActivity {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = ItemChooserActivity.class.getSimpleName();

    private static final int SELECT_ITEM = 100;
    public static final String EXTRA_ITEM = "item";
    public static final String EXTRA_IS_FROM_CALCULATOR = "calculator";

    public static final String[] EFFECT_COLUMNS = {
            UnusualSchemaEntry._ID,
            UnusualSchemaEntry.COLUMN_ID,
            UnusualSchemaEntry.COLUMN_NAME
    };

    public static final int COLUMN_INDEX = 1;
    public static final int COLUMN_NAME = 2;

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    @Bind(R.id.quality) Spinner qualitySpinner;
    @Bind(R.id.effect) Spinner effectSpinner;
    @Bind(R.id.weapon_wear) Spinner wearSpinner;
    @Bind(R.id.title_effect) TextView titleEffect;
    @Bind(R.id.title_wear) TextView titleWear;
    @Bind(R.id.icon) ImageView icon;
    @Bind(R.id.item_text) TextView itemText;
    @Bind(R.id.item_name) TextView itemName;
    @Bind(R.id.tradable) CheckBox tradable;
    @Bind(R.id.craftable) CheckBox craftable;
    @Bind(R.id.australium) CheckBox australium;
    @Bind(R.id.fab) FloatingActionButton fab;

    private Cursor effectCursor;

    private Item item;

    private QualityAdapter qualityAdapter;
    private EffectAdapter effectAdapter;
    private WeaponWearAdapter wearAdapter;

    private boolean isFromCalculator = false;

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
        setTitle(null);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        isFromCalculator = getIntent().getBooleanExtra(EXTRA_IS_FROM_CALCULATOR, false);

        //Show the home button as back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        qualityAdapter = new QualityAdapter(this);
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
        effectAdapter = new EffectAdapter(this, effectCursor);
        effectSpinner.setAdapter(effectAdapter);

        wearAdapter = new WeaponWearAdapter(this);
        wearSpinner.setAdapter(wearAdapter);

        item = new Item();

        fab.hide();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_ITEM:
                if (resultCode == RESULT_OK) {
                    item.setDefindex(data.getIntExtra(SelectItemActivity.EXTRA_DEFINDEX, -1));
                    item.setName(data.getStringExtra(SelectItemActivity.EXTRA_NAME));
                    updateItemIcon();
                    fab.show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.fab)
    public void submit() {
        item.setQuality(qualityAdapter.getQualityId(qualitySpinner.getSelectedItemPosition()));
        if (item.getQuality() == Quality.UNUSUAL) {
            item.setPriceIndex(effectAdapter.getEffectId(effectSpinner.getSelectedItemPosition()));
        } else if (item.getQuality() == Quality.PAINTKITWEAPON) {
            item.setWeaponWear(wearAdapter.getWearId(wearSpinner.getSelectedItemPosition()));
        }

        item.setTradable(tradable.isChecked());
        item.setCraftable(craftable.isChecked());
        item.setAustralium(australium.isChecked());

        if (isFromCalculator) {
            Cursor cursor = getContentResolver().query(
                    CalculatorEntry.CONTENT_URI,
                    null,
                    CalculatorEntry.COLUMN_DEFINDEX + " = ? AND " +
                            CalculatorEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                            CalculatorEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                            CalculatorEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                            CalculatorEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                            CalculatorEntry.COLUMN_AUSTRALIUM + " = ? AND " +
                            CalculatorEntry.COLUMN_WEAPON_WEAR + " = ?",
                    new String[]{String.valueOf(item.getDefindex()),
                            String.valueOf(item.getQuality()),
                            item.isTradable() ? "1" : "0",
                            item.isCraftable() ? "1" : "0",
                            String.valueOf(item.getPriceIndex()),
                            item.isAustralium() ? "1" : "0",
                            String.valueOf(item.getWeaponWear())
                    },
                    null
            );

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.close();
                    Toast.makeText(this, "You have already added this item", Toast.LENGTH_SHORT).show();
                    return;
                }
                cursor.close();
            }
        }

        Intent result = new Intent();
        result.putExtra(EXTRA_ITEM, item);
        setResult(RESULT_OK, result);
        finish();
    }

    @OnClick(R.id.item)
    public void selectItem() {
        startActivityForResult(new Intent(this, SelectItemActivity.class), SELECT_ITEM);
    }

    private void updateItemIcon() {
        icon.setVisibility(View.VISIBLE);
        itemText.setVisibility(View.GONE);
        itemName.setVisibility(View.VISIBLE);
        itemName.setText(item.getName());
        Glide.with(this)
                .load(item.getIconUrl(this))
                .into(icon);
    }
}
