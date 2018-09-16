package com.tlongdev.bktf.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.RecentsAdapter;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.presenter.fragment.RecentsPresenter;
import com.tlongdev.bktf.ui.activity.SearchActivity;
import com.tlongdev.bktf.ui.view.fragment.RecentsView;
import com.tlongdev.bktf.util.Utility;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * recents fragment. Shows a list of all the prices orderd by the time of the price update.
 */
public class RecentsFragment extends BptfFragment implements RecentsView,
        SwipeRefreshLayout.OnRefreshListener, RecentsAdapter.OnMoreListener {

    @Inject RecentsPresenter mPresenter;

    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.text_view_metal_price) TextView mMetalPrice;
    @BindView(R.id.text_view_key_price) TextView mKeyPrice;
    @BindView(R.id.text_view_buds_price) TextView mBudsPrice;
    @BindView(R.id.image_view_metal_price) View metalPriceImage;
    @BindView(R.id.image_view_key_price) View keyPriceImage;
    @BindView(R.id.image_view_buds_price) View budsPriceImage;

    /**
     * Adapter of the recycler view
     */
    private RecentsAdapter mAdapter;

    private ProgressDialog loadingDialog;

    private Context mContext;

    private Unbinder mUnbinder;

    /**
     * Constructor
     */
    public RecentsFragment() {
        //Required empty constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recents, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mApplication.getFragmentComponent().inject(this);

        mPresenter.attachView(this);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) mContext).setSupportActionBar(rootView.findViewById(R.id.toolbar));

        mAdapter = new RecentsAdapter(mApplication);
        mAdapter.setListener(this);

        //Setup the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setVisibility(View.GONE);

        //Set up the swipe refresh layout (color and listener)
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (savedInstanceState != null) {
            updateCurrencyHeader(
                    savedInstanceState.getParcelable(RecentsPresenter.STATE_METAL),
                    savedInstanceState.getParcelable(RecentsPresenter.STATE_KEY),
                    savedInstanceState.getParcelable(RecentsPresenter.STATE_BUDS)
            );
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.loadPrices();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
        mUnbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mPresenter.saveCurrencyPrices(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recents, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //Start the search activity
                startActivity(new Intent(mContext, SearchActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        mPresenter.downloadPrices();
    }

    @Override
    public void showPrices(Cursor prices) {
        mAdapter.changeCursor(prices);
        mRecyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showRefreshAnimation() {
        //Workaround for the circle not appearing
        mSwipeRefreshLayout.post(() -> {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void hideRefreshingAnimation() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void updateCurrencyHeader(Price metalPrice, Price keyPrice, Price budPrice) {
        if (metalPrice != null) {
            mMetalPrice.setText(metalPrice.getFormattedPrice(getActivity()));
            if (metalPrice.getDifference() > 0.0) {
                metalPriceImage.setBackgroundColor(0xff008504);
            } else {
                metalPriceImage.setBackgroundColor(0xff850000);
            }
        }
        if (keyPrice != null) {
            mKeyPrice.setText(keyPrice.getFormattedPrice(getActivity()));
            if (keyPrice.getDifference() > 0.0) {
                keyPriceImage.setBackgroundColor(0xff008504);
            } else {
                keyPriceImage.setBackgroundColor(0xff850000);
            }
        }

        if (budPrice != null) {
            mBudsPrice.setText(budPrice.getFormattedPrice(getActivity()));
            if (budPrice.getDifference() > 0) {
                budsPriceImage.setBackgroundColor(0xff008504);
            } else {
                budsPriceImage.setBackgroundColor(0xff850000);
            }
        }
    }

    @Override
    public void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    @Override
    public void showLoadingDialog(String message) {
        loadingDialog = ProgressDialog.show(getActivity(), null, message, true);
        loadingDialog.setCancelable(false);
    }

    @Override
    public void updateLoadingDialog(int max, String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            if (loadingDialog.isIndeterminate()) {
                loadingDialog.dismiss();
                loadingDialog = new ProgressDialog(mContext);
                loadingDialog.setIndeterminate(false);
                loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                loadingDialog.setMessage(message);
                loadingDialog.setMax(max);
                loadingDialog.setCancelable(false);
                loadingDialog.show();
            } else {
                loadingDialog.incrementProgressBy(1);
            }
        }
    }

    @Override
    public void showItemSchemaError(String errorMessage) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            showErrorDialog();
        } else {
            showToast("bptf: " + errorMessage, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void showPricesError(String errorMessage) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            showErrorDialog();
        } else {
            showToast("bptf: " + errorMessage, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void showErrorDialog() {
        //Quit the app if the download failed.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mContext.getString(R.string.message_database_fail_network)).setCancelable(false).
                setPositiveButton(mContext.getString(R.string.action_close),
                        (dialog, which) -> getActivity().finish());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onMoreClicked(View view, Item item) {
        Utility.createItemPopupMenu(getActivity(), view, item).show();
    }
}
