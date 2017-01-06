/**
 * Copyright 2015 Long Tran
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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
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
import android.widget.PopupMenu;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.FavoritesAdapter;
import com.tlongdev.bktf.customtabs.CustomTabActivityHelper;
import com.tlongdev.bktf.customtabs.WebViewFallback;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.presenter.fragment.FavoritesPresenter;
import com.tlongdev.bktf.ui.activity.ItemChooserActivity;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.PriceHistoryActivity;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.ui.view.fragment.FavoritesView;
import com.tlongdev.bktf.util.Utility;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends BptfFragment implements FavoritesView,
        FavoritesAdapter.OnMoreListener {

    @Inject FavoritesPresenter mPresenter;

    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.ad_view) AdView mAdView;

    private FavoritesAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * Constructor
     */
    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mApplication.getFragmentComponent().inject(this);

        mPresenter.attachView(this);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        mAdapter = new FavoritesAdapter(mApplication);
        mAdapter.setListener(this);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int columnCount = 1;
        if (dpWidth >= 720) {
            columnCount = 3;
        } else if (dpWidth >= 600) {
            columnCount = 2;
        }

        //Setup the recycler view
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        mRecyclerView.setAdapter(mAdapter);

        mAdManager.addAdView(mAdView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(FavoritesFragment.class.getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdManager.removeAdView(mAdView);
        mPresenter.detachView();
        mUnbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MainActivity.REQUEST_NEW_ITEM:
                if (resultCode == Activity.RESULT_OK) {
                    Utility.addToFavorites(getActivity(), (Item) data.getParcelableExtra(ItemChooserActivity.EXTRA_ITEM));
                    mPresenter.loadFavorites();
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorites, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //Start the search activity
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.action_add_currencies:
                mPresenter.addCurrencies();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab)
    public void addItem() {
        startActivityForResult(new Intent(getActivity(), ItemChooserActivity.class), MainActivity.REQUEST_NEW_ITEM);
    }

    @Override
    public void showFavorites(List<Item> items) {
        mAdapter.setDataSet(items);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMoreClicked(View view, final Item item) {
        PopupMenu menu = new PopupMenu(getActivity(), view);
        menu.getMenuInflater().inflate(R.menu.popup_item, menu.getMenu());
        menu.getMenu().findItem(R.id.favorite).setTitle("Remove from favorites");
        menu.getMenu().findItem(R.id.calculator).setEnabled(!Utility.isInCalculator(getActivity(), item));
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.history:
                        intent = new Intent(getActivity(), PriceHistoryActivity.class);
                        intent.putExtra(PriceHistoryActivity.EXTRA_ITEM, item);
                        startActivity(intent);
                        break;
                    case R.id.favorite:
                        Utility.removeFromFavorites(getActivity(), item);
                        mAdapter.removeItem(item);
                        break;
                    case R.id.calculator:
                        Utility.addToCalculator(getActivity(), item);
                        menuItem.setEnabled(false);
                        break;
                    case R.id.backpack_tf:
                        CustomTabActivityHelper.openCustomTab(getActivity(),
                                new CustomTabsIntent.Builder().build(),
                                Uri.parse(item.getBackpackTfUrl()),
                                new WebViewFallback());
                        break;
                    case R.id.wiki:
                        CustomTabActivityHelper.openCustomTab(getActivity(),
                                new CustomTabsIntent.Builder().build(),
                                Uri.parse(item.getTf2WikiUrl()),
                                new WebViewFallback());
                        break;
                    case R.id.tf2outpost:
                        intent = new Intent(Intent.ACTION_VIEW, Utility.buildTf2OutpostSearchUrl(getActivity(), item));
                        getActivity().startActivity(intent);
                        break;
                    case R.id.bazaar_tf:
                        intent = new Intent(Intent.ACTION_VIEW, Utility.buildBazaarSearchUrl(getActivity(), item));
                        getActivity().startActivity(intent);
                        break;
                }
                return true;
            }
        });

        menu.show();
    }
}
