package com.tlongdev.bktf.fragment;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.tlongdev.bktf.util.Utility;
import com.tlongdev.bktf.activity.ItemChooserActivity;
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.adapter.CalculatorAdapter;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Price;

import java.util.ArrayList;

/**
 * Calculator fragment. Let's the user create a list of items and it will calculate the total value
 * of the items
 */
public class CalculatorFragment extends Fragment implements MainActivity.OnDrawerOpenedListener {

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
     * the selection
     */
    public static final String mSelection = PriceEntry.TABLE_NAME +
            "." + PriceEntry._ID + " = ?";

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    /**
     * the adapter of the recycler view
     */
    private CalculatorAdapter mAdapter;

    /**
     * Pairs of prices table IDs and count numbers.
     */
    private ArrayList<Utility.IntegerPair> ids = new ArrayList<>();

    /**
     * Views of the results
     */
    private TextView priceMetal;
    private TextView priceKeys;
    private TextView priceBuds;
    private TextView priceUsd;

    /**
     * The sum of the price of items in the list
     */
    private Price totalPrice = new Price();

    /**
     * Only needed for manually expanding the toolbar
     */
    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;

    /**
     * Constrctor.
     */
    public CalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        PRICE_LIST_COLUMNS[COL_PRICE_LIST_PRAW] = Utility.getRawPriceQueryString(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        mTracker = application.getDefaultTracker();

        View rootView = inflater.inflate(R.layout.fragment_calculator, container, false);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Views used for toolbar behavior
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar_layout);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);

        //Init the recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        mAdapter = new CalculatorAdapter(getActivity(), ids);
        mAdapter.setOnItemDeletedListener(new CalculatorAdapter.OnItemEditListener() {
            @Override
            public void onItemDeleted(int id, int count) {
                deleteItem(id, count);
            }
        });

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        //Init the FAB
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ItemChooserActivity.class);
                startActivityForResult(i, MainActivity.REQUEST_NEW_ITEM);
                getActivity().overridePendingTransition(R.anim.item_chooser_animation, 0);
            }
        });

        totalPrice.setCurrency(Currency.METAL);

        priceMetal = (TextView) rootView.findViewById(R.id.text_view_price_metal);
        priceKeys = (TextView) rootView.findViewById(R.id.text_view_price_keys);
        priceBuds = (TextView) rootView.findViewById(R.id.text_view_price_buds);
        priceUsd = (TextView) rootView.findViewById(R.id.text_view_price_usd);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MainActivity.REQUEST_NEW_ITEM) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                int id = data.getIntExtra(ItemChooserActivity.EXTRA_ITEM_ID, -1);
                if (id >= 0) {
                    ids.add(new Utility.IntegerPair(id, data.getIntExtra(ItemChooserActivity.EXTRA_ITEM_COUNT, 0)));
                    mAdapter.notifyDataSetChanged();
                    addItem(id, data.getIntExtra(ItemChooserActivity.EXTRA_ITEM_COUNT, 0));
                }
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
                ids.clear();
                mAdapter.notifyDataSetChanged();
                deleteAllItems();
                break;
        }
        return true;
    }

    /**
     * Deletes all the items from the list
     */
    private void deleteAllItems() {
        totalPrice.setValue(0);
        try {
            priceMetal.setText(totalPrice.getFormattedPrice(getActivity(), Currency.METAL));
            priceKeys.setText(totalPrice.getFormattedPrice(getActivity(), Currency.KEY));
            priceBuds.setText(totalPrice.getFormattedPrice(getActivity(), Currency.BUD));
            priceUsd.setText(totalPrice.getFormattedPrice(getActivity(), Currency.USD));
        } catch (Throwable t) {
            t.printStackTrace();

            ((BptfApplication)getActivity().getApplication()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Formatter exception:CalculatorFragment, Message: " + t.getMessage())
                    .setFatal(false)
                    .build());
        }
    }

    /**
     * Adds an element into the list
     *
     * @param id    the id of the item
     * @param count the number of the item(s)
     */
    private void addItem(int id, int count) {
        Cursor cursor = getActivity().getContentResolver().query(
                PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                mSelection,
                new String[]{"" + id},
                null
        );

        //The value to the total price
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalPrice.setValue(totalPrice.getValue() + cursor.getDouble(COL_PRICE_LIST_PRAW) * count);
            }
            cursor.close();
        }

        try {
            priceMetal.setText(totalPrice.getFormattedPrice(getActivity(), Currency.METAL));
            priceKeys.setText(totalPrice.getFormattedPrice(getActivity(), Currency.KEY));
            priceBuds.setText(totalPrice.getFormattedPrice(getActivity(), Currency.BUD));
            priceUsd.setText(totalPrice.getFormattedPrice(getActivity(), Currency.USD));
        } catch (Throwable t) {
            t.printStackTrace();

            ((BptfApplication)getActivity().getApplication()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Formatter exception:CalculatorFragment, Message: " + t.getMessage())
                    .setFatal(false)
                    .build());
        }
    }

    /**
     * Deletes a single element from the list
     *
     * @param id    the id of the item
     * @param count the number of the item(s)
     */
    private void deleteItem(int id, int count) {
        Cursor cursor = getActivity().getContentResolver().query(
                PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                mSelection,
                new String[]{"" + id},
                null
        );

        //Subtract the value from the sum
        if (cursor != null && cursor.moveToFirst()) {
            totalPrice.setValue(totalPrice.getValue() - cursor.getDouble(COL_PRICE_LIST_PRAW) * count);
            cursor.close();
        }

        try {
            priceMetal.setText(totalPrice.getFormattedPrice(getActivity(), Currency.METAL));
            priceKeys.setText(totalPrice.getFormattedPrice(getActivity(), Currency.KEY));
            priceBuds.setText(totalPrice.getFormattedPrice(getActivity(), Currency.BUD));
            priceUsd.setText(totalPrice.getFormattedPrice(getActivity(), Currency.USD));
        } catch (Throwable t) {
            t.printStackTrace();

            ((BptfApplication)getActivity().getApplication()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Formatter exception:CalculatorFragment, Message: " + t.getMessage())
                    .setFatal(false)
                    .build());
        }
    }

    /**
     * Fully expand the toolbar with animation.
     */
    public void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }
}
