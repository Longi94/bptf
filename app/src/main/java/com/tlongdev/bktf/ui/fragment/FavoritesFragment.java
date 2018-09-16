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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.FavoritesAdapter;
import com.tlongdev.bktf.customtabs.CustomTabActivityHelper;
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
        ((AppCompatActivity) getActivity()).setSupportActionBar(rootView.findViewById(R.id.toolbar));

        mAdapter = new FavoritesAdapter(mApplication);
        mAdapter.setListener(this);

        //Setup the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.loadFavorites();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
        mUnbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MainActivity.REQUEST_NEW_ITEM:
                if (resultCode == Activity.RESULT_OK) {
                    Utility.addToFavorites(getActivity(), data.getParcelableExtra(ItemChooserActivity.EXTRA_ITEM));
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
        menu.setOnMenuItemClickListener(menuItem -> {
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
                            Uri.parse(item.getBackpackTfUrl()));
                    break;
                case R.id.wiki:
                    CustomTabActivityHelper.openCustomTab(getActivity(),
                            new CustomTabsIntent.Builder().build(),
                            Uri.parse(item.getTf2WikiUrl()));
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
        });

        menu.show();
    }
}
