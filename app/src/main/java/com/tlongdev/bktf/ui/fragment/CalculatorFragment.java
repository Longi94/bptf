/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.CalculatorAdapter;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.fragment.CalculatorPresenter;
import com.tlongdev.bktf.ui.activity.ItemChooserActivity;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.ui.view.fragment.CalculatorView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Calculator fragment. Let's the user create a list of items and it will calculate the total value
 * of the items
 */
public class CalculatorFragment extends BptfFragment implements CalculatorView, MainActivity.OnDrawerOpenedListener{

    @BindView(R.id.text_view_price_metal) TextView priceMetal;
    @BindView(R.id.text_view_price_keys) TextView priceKeys;
    @BindView(R.id.text_view_price_buds) TextView priceBuds;
    @BindView(R.id.text_view_price_usd) TextView priceUsd;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.ad_view) AdView mAdView;

    private CalculatorAdapter mAdapter;
    private CalculatorPresenter mPresenter;
    private Unbinder mUnbinder;

    public CalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPresenter = new CalculatorPresenter(mApplication);
        mPresenter.attachView(this);

        View rootView = inflater.inflate(R.layout.fragment_calculator, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        mAdapter = new CalculatorAdapter(mApplication);
        mAdapter.setListener(mPresenter);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int columnCount = 1;
        if (dpWidth >= 720) {
            columnCount = 3;
        } else if (dpWidth >= 600) {
            columnCount = 2;
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        mRecyclerView.setAdapter(mAdapter);

        priceMetal.setText(getString(R.string.currency_metal, "0"));
        priceKeys.setText(getString(R.string.currency_key_plural, "0"));
        priceBuds.setText(getString(R.string.currency_bud_plural, "0"));
        priceUsd.setText("$0");

        mAdManager.addAdView(mAdView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.loadItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(CalculatorFragment.class.getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
        mAdManager.removeAdView(mAdView);
        mUnbinder.unbind();
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
                Item item = data.getParcelableExtra(ItemChooserActivity.EXTRA_ITEM);
                mPresenter.addItem(item);
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
                mPresenter.clearItems();
                break;
        }
        return true;
    }

    @Override
    public void onDrawerOpened() {
        expandToolbar();
    }

    @Override
    public void updatePrices(Price totalPrice) {
        try {
            priceMetal.setText(totalPrice.getFormattedPrice(getActivity(), Currency.METAL));
            priceKeys.setText(totalPrice.getFormattedPrice(getActivity(), Currency.KEY));
            priceBuds.setText(totalPrice.getFormattedPrice(getActivity(), Currency.BUD));
            priceUsd.setText(totalPrice.getFormattedPrice(getActivity(), Currency.USD));
        } catch (Throwable t) {
            Crashlytics.logException(t);
        }
    }

    /**
     * Fully expand the toolbar with animation.
     */
    private void expandToolbar() {
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) ((CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams()).getBehavior();
        behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, -1000, true);
    }

    @Override
    public void showItems(List<Item> items, List<Integer> count, Price totalPrice) {
        updatePrices(totalPrice);
        mAdapter.setDataSet(items, count);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void clearItems() {
        int count = mAdapter.getItemCount();
        mAdapter.clearDataSet();
        mAdapter.notifyItemRangeRemoved(0, count);
    }
}
