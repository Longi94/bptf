package com.tlongdev.bktf.fragment;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tlongdev.bktf.ItemChooserActivity;
import com.tlongdev.bktf.MainActivity;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.adapter.AdvancedCalculatorAdapter;
import com.tlongdev.bktf.data.DatabaseContract.PriceEntry;

import java.util.ArrayList;

/**
 * Fragment for calculator
 */
public class AdvancedCalculatorFragment extends Fragment {

    private static final String[] PRICE_LIST_COLUMNS = {
            PriceEntry.TABLE_NAME + "." + PriceEntry._ID,
            null,
    };

    //Indexes for the columns above
    public static final int COL_PRICE_LIST_PRAW = 1;

    public static final String mSelection = PriceEntry.TABLE_NAME +
            "." + PriceEntry._ID + " = ?";

    private AdvancedCalculatorAdapter mAdapter;

    private ArrayList<Utility.IntegerPair> ids = new ArrayList<>();

    private TextView priceMetal;
    private TextView priceKeys;
    private TextView priceBuds;
    private TextView priceUsd;

    private double totalPrice = 0;

    public AdvancedCalculatorFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_advanced_calculator, container, false);
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new AdvancedCalculatorAdapter(getActivity(), ids);
        mAdapter.setOnItemDeletedListener(new AdvancedCalculatorAdapter.OnItemEditListener() {
            @Override
            public void onItemDeleted(int id, int count) {
                deleteItem(id, count);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        rootView.findViewById(R.id.image_button_add).setOnClickListener(new View.OnClickListener() {
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
        inflater.inflate(R.menu.menu_advanced_calculator, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_show_simple:
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putBoolean(getString(R.string.pref_preferred_advanced_calculator), false).apply();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.simple_fade_in, R.anim.simple_fade_out)
                        .replace(R.id.container, new SimpleCalculatorFragment())
                        .commit();
                break;
            case R.id.action_clear:
                ids.clear();
                mAdapter.notifyDataSetChanged();
                deleteAllItems();
                break;
        }
        return true;
    }

    private void deleteAllItems() {
        totalPrice = 0;
        try {
            priceMetal.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_METAL, false));
            priceKeys.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_KEY, false));
            priceBuds.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_BUD, false));
            priceUsd.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_USD, false));
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
                    Utility.CURRENCY_METAL, Utility.CURRENCY_METAL, false));
            priceKeys.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_KEY, false));
            priceBuds.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_BUD, false));
            priceUsd.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_USD, false));
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
                    Utility.CURRENCY_METAL, Utility.CURRENCY_METAL, false));
            priceKeys.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_KEY, false));
            priceBuds.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_BUD, false));
            priceUsd.setText(Utility.formatPrice(getActivity(), totalPrice, 0,
                    Utility.CURRENCY_METAL, Utility.CURRENCY_USD, false));
        } catch (Throwable throwable) {
            if (Utility.isDebugging(getActivity())) {
                throwable.printStackTrace();
            }
        }
    }
}
