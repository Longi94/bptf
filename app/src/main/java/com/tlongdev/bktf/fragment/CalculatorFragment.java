package com.tlongdev.bktf.fragment;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.activity.ItemChooserActivity;
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.adapter.CalculatorAdapter;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.CalculatorEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Calculator fragment. Let's the user create a list of items and it will calculate the total value
 * of the items
 */
public class CalculatorFragment extends Fragment implements MainActivity.OnDrawerOpenedListener, LoaderManager.LoaderCallbacks<Cursor>, CalculatorAdapter.OnItemEditListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = CalculatorFragment.class.getSimpleName();

    /**
     * The columns to query
     */
    private static final String[] PRICE_LIST_COLUMNS = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            null,
    };

    /**
     * Indexes for the columns above
     */
    public static final int COL_PRICE_LIST_PRAW = 1;

    /**
     * Indexes
     */
    public static final int COLUMN_DEFINDEX = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_QUALITY = 2;
    public static final int COLUMN_TRADABLE = 3;
    public static final int COLUMN_CRAFTABLE = 4;
    public static final int COLUMN_PRICE_INDEX = 5;
    public static final int COLUMN_COUNT = 6;
    public static final int COLUMN_CURRENCY = 7;
    public static final int COLUMN_PRICE = 8;
    public static final int COLUMN_PRICE_MAX = 9;
    public static final int COLUMN_PRICE_RAW = 10;
    public static final int COLUMN_DIFFERENCE = 11;
    public static final int COLUMN_AUSTRALIUM = 12;

    /**
     * the selection
     */
    public static final String mSelection =
            PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM + " = ? AND " +
                    PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_WEAPON_WEAR + " = ?";

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    /**
     * the adapter of the recycler view
     */
    private CalculatorAdapter mAdapter;

    /**
     * Views of the results
     */
    @Bind(R.id.text_view_price_metal) TextView priceMetal;
    @Bind(R.id.text_view_price_keys) TextView priceKeys;
    @Bind(R.id.text_view_price_buds) TextView priceBuds;
    @Bind(R.id.text_view_price_usd) TextView priceUsd;

    /**
     * The sum of the price of items in the list
     */
    private Price totalPrice = new Price();

    /**
     * Only needed for manually expanding the toolbar
     */
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;

    /**
     * Constructor.
     */
    public CalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        PRICE_LIST_COLUMNS[COL_PRICE_LIST_PRAW] = Utility.getRawPriceQueryString(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        mTracker = application.getDefaultTracker();

        View rootView = inflater.inflate(R.layout.fragment_calculator, container, false);
        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        mAdapter = new CalculatorAdapter(getActivity(), null);
        mAdapter.setListener(this);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        totalPrice.setCurrency(Currency.METAL);

        priceMetal.setText(getString(R.string.currency_metal, "0"));
        priceKeys.setText(getString(R.string.currency_key_plural, "0"));
        priceBuds.setText(getString(R.string.currency_bud_plural, "0"));
        priceUsd.setText("$0");

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Calculator");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @OnClick(R.id.fab)
    public void addItem() {
        Intent i = new Intent(getActivity(), ItemChooserActivity.class);
        i.putExtra(ItemChooserActivity.EXTRA_IS_FROM_CALCULATOR, true);
        startActivityForResult(i, MainActivity.REQUEST_NEW_ITEM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MainActivity.REQUEST_NEW_ITEM) {
            if (resultCode == Activity.RESULT_OK) {
                Item item = (Item) data.getSerializableExtra(ItemChooserActivity.EXTRA_ITEM);
                Cursor cursor = getActivity().getContentResolver().query(
                        PriceEntry.CONTENT_URI,
                        PRICE_LIST_COLUMNS,
                        mSelection,
                        new String[]{
                                String.valueOf(item.getDefindex()),
                                String.valueOf(item.getQuality()),
                                String.valueOf(item.isTradable() ? 1 : 0),
                                String.valueOf(item.isCraftable() ? 1 : 0),
                                String.valueOf(item.getPriceIndex()),
                                String.valueOf(item.isAustralium() ? 1 : 0),
                                String.valueOf(item.getWeaponWear()),
                        },
                        null
                );

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        Price price = new Price();
                        price.setRawValue(cursor.getDouble(COL_PRICE_LIST_PRAW));
                        item.setPrice(price);
                    }
                    cursor.close();
                }

                addItem(item, 1);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_calculator, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_search:
                //Start the search activity
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.action_clear:
                //Clear the items
                deleteAllItems();
                break;
        }
        return true;
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sql = "SELECT " +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_DEFINDEX + "," +
                ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_ITEM_NAME + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_QUALITY + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_TRADABLE + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_CRAFTABLE + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_PRICE_INDEX + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_COUNT + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_CURRENCY + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_HIGH + "," +
                Utility.getRawPriceQueryString(getActivity()) + "," +
                PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DIFFERENCE + "," +
                CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_AUSTRALIUM +
                " FROM " + CalculatorEntry.TABLE_NAME +
                " LEFT JOIN " + PriceEntry.TABLE_NAME +
                " ON " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_DEFINDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_DEFINDEX +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_TRADABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_TRADABLE +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_CRAFTABLE + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_CRAFTABLE +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_PRICE_INDEX + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_PRICE_INDEX +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_ITEM_QUALITY + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_ITEM_QUALITY +
                " AND " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_AUSTRALIUM + " = " + PriceEntry.TABLE_NAME + "." + PriceEntry.COLUMN_AUSTRALIUM +
                " LEFT JOIN " + ItemSchemaEntry.TABLE_NAME +
                " ON " + CalculatorEntry.TABLE_NAME + "." + CalculatorEntry.COLUMN_DEFINDEX + " = " + ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX +
                " ORDER BY " + ItemSchemaEntry.COLUMN_ITEM_NAME + " ASC";

        return new CursorLoader(
                getActivity(),
                DatabaseContract.RAW_QUERY_URI,
                null,
                sql,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<Item> items = new ArrayList<>();
        ArrayList<Integer> count = new ArrayList<>();

        double value = 0;

        if (data != null) {
            while (data.moveToNext()) {
                Item item = new Item(data.getInt(COLUMN_DEFINDEX),
                        data.getString(COLUMN_NAME),
                        data.getInt(COLUMN_QUALITY),
                        data.getInt(COLUMN_TRADABLE) == 1,
                        data.getInt(COLUMN_CRAFTABLE) == 1,
                        data.getInt(COLUMN_AUSTRALIUM) == 1,
                        data.getInt(COLUMN_PRICE_INDEX),
                        null
                );

                if (data.getString(COLUMN_CURRENCY) != null) {
                    item.setPrice(new Price(data.getDouble(COLUMN_PRICE),
                            data.getDouble(COLUMN_PRICE_MAX),
                            data.getDouble(COLUMN_PRICE_RAW),
                            0,
                            data.getDouble(COLUMN_DIFFERENCE),
                            data.getString(COLUMN_CURRENCY)));

                    value += item.getPrice().getRawValue() * data.getInt(COLUMN_COUNT);
                }

                items.add(item);

                count.add(data.getInt(COLUMN_COUNT));
            }
        }

        totalPrice.setValue(value);
        updatePrices();

        mAdapter.setDataSet(items, count);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setDataSet(null, null);
        mAdapter.notifyDataSetChanged();
    }

    private void updatePrices() {
        try {
            priceMetal.setText(totalPrice.getFormattedPrice(getActivity(), Currency.METAL));
            priceKeys.setText(totalPrice.getFormattedPrice(getActivity(), Currency.KEY));
            priceBuds.setText(totalPrice.getFormattedPrice(getActivity(), Currency.BUD));
            priceUsd.setText(totalPrice.getFormattedPrice(getActivity(), Currency.USD));
        } catch (Throwable t) {
            t.printStackTrace();

            ((BptfApplication) getActivity().getApplication()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Formatter exception:CalculatorFragment, Message: " + t.getMessage())
                    .setFatal(false)
                    .build());
        }
    }

    /**
     * Deletes all the items from the list
     */
    private void deleteAllItems() {
        totalPrice.setValue(0);
        updatePrices();

        getActivity().getContentResolver().delete(CalculatorEntry.CONTENT_URI, null, null);
        int count = mAdapter.getItemCount();
        mAdapter.clearDataSet();
        mAdapter.notifyItemRangeRemoved(0, count);
    }

    /**
     * Adds an element into the list
     *
     * @param item  the id of the item
     * @param count the number of the item(s)
     */
    private void addItem(Item item, int count) {

        if (item.getPrice() != null) {
            totalPrice.setValue(totalPrice.getValue() + item.getPrice().getRawValue() * count);
        }

        updatePrices();

        ContentValues cv = new ContentValues();

        cv.put(CalculatorEntry.COLUMN_DEFINDEX, item.getDefindex());
        cv.put(CalculatorEntry.COLUMN_ITEM_QUALITY, item.getQuality());
        cv.put(CalculatorEntry.COLUMN_ITEM_TRADABLE, item.isTradable() ? 1 : 0);
        cv.put(CalculatorEntry.COLUMN_ITEM_CRAFTABLE, item.isCraftable() ? 1 : 0);
        cv.put(CalculatorEntry.COLUMN_PRICE_INDEX, item.getPriceIndex());
        cv.put(CalculatorEntry.COLUMN_AUSTRALIUM, item.isAustralium() ? 1 : 0);
        cv.put(CalculatorEntry.COLUMN_WEAPON_WEAR, item.getWeaponWear());
        cv.put(CalculatorEntry.COLUMN_COUNT, 1);

        getActivity().getContentResolver().insert(CalculatorEntry.CONTENT_URI, cv);

        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

    @Override
    public void onItemDeleted(Item item, int count) {
        if (item.getPrice() != null) {
            totalPrice.setValue(totalPrice.getValue() - item.getPrice().getRawValue() * count);
        }

        updatePrices();

        getActivity().getContentResolver().delete(CalculatorEntry.CONTENT_URI,
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
                });
    }

    @Override
    public void onItemEdited(Item item, int oldCount, int newCount) {
        int diff = newCount - oldCount;
        if (diff == 0) return;

        totalPrice.setValue(totalPrice.getValue() + diff * item.getPrice().getRawValue());
        updatePrices();

        ContentValues values = new ContentValues();
        values.put(CalculatorEntry.COLUMN_COUNT, newCount);

        getActivity().getContentResolver().update(CalculatorEntry.CONTENT_URI,
                values,
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
                });
    }
}
