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

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.activity.ItemChooserActivity;
import com.tlongdev.bktf.activity.MainActivity;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.adapter.CalculatorAdapter;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;
import com.tlongdev.bktf.enums.Currency;

import java.util.ArrayList;

/**
 * Fragment for calculator
 */
public class CalculatorFragment extends Fragment implements MainActivity.OnDrawerOpenedListener {

    private static final String[] PRICE_LIST_COLUMNS = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            null,
    };

    //Indexes for the columns above
    public static final int COL_PRICE_LIST_PRAW = 1;

    public static final String mSelection = PriceEntry.TABLE_NAME +
            "." + PriceEntry._ID + " = ?";

    private CalculatorAdapter mAdapter;

    private ArrayList<Utility.IntegerPair> ids = new ArrayList<>();

    private TextView priceMetal;
    private TextView priceKeys;
    private TextView priceBuds;
    private TextView priceUsd;

    private double totalPrice = 0;

    private FloatingActionButton fab;

    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;

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
        View rootView = inflater.inflate(R.layout.fragment_calculator, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Views used for toolbar behavior
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar_layout);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CalculatorAdapter(getActivity(), ids);
        mAdapter.setOnItemDeletedListener(new CalculatorAdapter.OnItemEditListener() {
            @Override
            public void onItemDeleted(int id, int count) {
                deleteItem(id, count);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ItemChooserActivity.class);
                startActivityForResult(i, MainActivity.REQUEST_NEW_ITEM);
                getActivity().overridePendingTransition(R.anim.item_chooser_animation, 0);
            }
        });

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
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
        }
        return true;
    }

    private void deleteAllItems() {
        totalPrice = 0;
        try {
            priceMetal.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.METAL, false));
            priceKeys.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.KEY, false));
            priceBuds.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.BUD, false));
            priceUsd.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.USD, false));
        } catch (Throwable throwable) {
            if (Utility.isDebugging(getActivity())) {
                throwable.printStackTrace();
            }
        }
    }

    private void addItem(int id, int count) {
        Cursor cursor = getActivity().getContentResolver().query(
                PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                mSelection,
                new String[]{"" + id},
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            totalPrice += cursor.getDouble(COL_PRICE_LIST_PRAW) * count;
            cursor.close();
        }

        try {
            priceMetal.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.METAL, false));
            priceKeys.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.KEY, false));
            priceBuds.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.BUD, false));
            priceUsd.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.USD, false));
        } catch (Throwable throwable) {
            if (Utility.isDebugging(getActivity())) {
                throwable.printStackTrace();
            }
        }
    }

    private void deleteItem(int id, int count) {
        Cursor cursor = getActivity().getContentResolver().query(
                PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                mSelection,
                new String[]{"" + id},
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            totalPrice -= cursor.getDouble(COL_PRICE_LIST_PRAW) * count;
            cursor.close();
        }

        try {
            priceMetal.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.METAL, false));
            priceKeys.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.KEY, false));
            priceBuds.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.BUD, false));
            priceUsd.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Currency.METAL, Currency.USD, false));
        } catch (Throwable throwable) {
            if (Utility.isDebugging(getActivity())) {
                throwable.printStackTrace();
            }
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
