package com.tlongdev.bktf.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.adapter.CurrencyAdapter;
import com.tlongdev.bktf.model.CurrencyRates;
import com.tlongdev.bktf.presenter.fragment.CurrencyPresenter;
import com.tlongdev.bktf.ui.view.fragment.CurrencyView;
import com.tlongdev.bktf.util.CurrencyRatesManager;
import com.tlongdev.bktf.util.Utility;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CurrencyFragment extends DialogFragment implements CurrencyView {

    private static final String ARG_USD_VALUE = "usd";

    @Inject CurrencyPresenter mPresenter;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.base) TextView mBaseText;
    @BindView(R.id.age) TextView mAgeText;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    private double mUsd;

    private CurrencyAdapter mAdapter;

    public CurrencyFragment() {
        // Required empty public constructor
    }

    public static CurrencyFragment newInstance(double usd) {
        CurrencyFragment fragment = new CurrencyFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_USD_VALUE, usd);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUsd = getArguments().getDouble(ARG_USD_VALUE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_currency, container, false);
        ButterKnife.bind(this, rootView);

        BptfApplication application = (BptfApplication) getActivity().getApplication();
        application.getFragmentComponent().inject(this);

        mPresenter.attachView(this);

        mAdapter = new CurrencyAdapter();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        mBaseText.setText(String.format("Base: %s USD", Utility.formatDouble(mUsd)));

        mPresenter.getCurrencyRates();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
    }

    @Override
    public void showToast(CharSequence message, int duration) {
        Toast.makeText(getActivity(), message, duration).show();
    }

    @Override
    public void showCurrencyRates(CurrencyRates rates) {
        mAdapter.setDataSet(rates.getRates(), mUsd);
        mAdapter.notifyDataSetChanged();

        long age = System.currentTimeMillis() - rates.getLastUpdate();

        if (age < CurrencyRatesManager.SIX_HOURS) {
            mAgeText.setText("Exchange rates are up to date.");
        } else {
            mAgeText.setText(String.format(Locale.ENGLISH,
                    "Exchange rates were last updated %d hours ago.", age / (1000 * 60 * 60)));
        }

        mProgressBar.setVisibility(View.GONE);
        mAgeText.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mBaseText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String errorMessage) {
        mAgeText.setText("Failed to download exchange rates. Try again later");
        showToast(errorMessage, Toast.LENGTH_SHORT);

        mProgressBar.setVisibility(View.GONE);
        mAgeText.setVisibility(View.VISIBLE);
    }
}
