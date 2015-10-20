package com.tlongdev.bktf.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.enums.Currency;

public class CalculatorFragment extends Fragment implements View.OnClickListener {

    private EditText inputEarbuds;
    private EditText inputKeys;
    private EditText inputMetal;
    private EditText inputUsd;

    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;

    public CalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_calculator, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        inputEarbuds = (EditText) rootView.findViewById(R.id.edit_text_earbuds);
        inputKeys = (EditText) rootView.findViewById(R.id.edit_text_keys);
        inputMetal = (EditText) rootView.findViewById(R.id.edit_text_metal);
        inputUsd = (EditText) rootView.findViewById(R.id.edit_text_usd);

        inputEarbuds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (inputEarbuds.isFocused()) {
                        if (s.toString().equals("")) {
                            inputKeys.setText(null);
                            inputMetal.setText(null);
                            inputUsd.setText(null);
                        } else {
                            inputKeys.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.BUD, Currency.KEY), 2)));
                            inputMetal.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.BUD, Currency.METAL), 2)));
                            inputUsd.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.BUD, Currency.USD), 2)));
                        }
                    }
                } catch (Throwable throwable) {
                    if (Utility.isDebugging(getActivity()))
                        throwable.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inputKeys.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (inputKeys.isFocused()) {
                        if (s.toString().equals("")) {
                            inputEarbuds.setText(null);
                            inputMetal.setText(null);
                            inputUsd.setText(null);
                        } else {
                            inputEarbuds.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.KEY, Currency.BUD), 2)));
                            inputMetal.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.KEY, Currency.METAL), 2)));
                            inputUsd.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.KEY, Currency.USD), 2)));
                        }
                    }
                } catch (Throwable throwable) {
                    if (Utility.isDebugging(getActivity()))
                        throwable.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inputMetal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (inputMetal.isFocused()) {
                        if (s.toString().equals("")) {
                            inputKeys.setText(null);
                            inputEarbuds.setText(null);
                            inputUsd.setText(null);
                        } else {
                            inputKeys.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.METAL, Currency.KEY), 2)));
                            inputEarbuds.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.METAL, Currency.BUD), 2)));
                            inputUsd.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.METAL, Currency.USD), 2)));
                        }
                    }
                } catch (Throwable throwable) {
                    if (Utility.isDebugging(getActivity()))
                        throwable.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inputUsd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (inputUsd.isFocused()) {
                        if (s.toString().equals("")) {
                            inputKeys.setText(null);
                            inputMetal.setText(null);
                            inputEarbuds.setText(null);
                        } else {
                            inputKeys.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.USD, Currency.KEY), 2)));
                            inputMetal.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.USD, Currency.METAL), 2)));
                            inputEarbuds.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Currency.USD, Currency.BUD), 2)));
                        }
                    }
                } catch (Throwable throwable) {
                    if (Utility.isDebugging(getActivity()))
                        throwable.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        inputEarbuds.setText("1");
        try {
            inputKeys.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                    1, Currency.BUD, Currency.KEY), 2)));
            inputMetal.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                    1, Currency.BUD, Currency.METAL), 2)));
            inputUsd.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                    1, Currency.BUD, Currency.USD), 2)));
        } catch (Throwable throwable) {
            if (Utility.isDebugging(getActivity()))
                throwable.printStackTrace();
        }

        rootView.findViewById(R.id.calculator_0).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_1).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_2).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_3).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_4).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_5).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_6).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_7).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_8).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_9).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_dot).setOnClickListener(this);
        rootView.findViewById(R.id.calculator_delete).setOnClickListener(this);
        return rootView;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }
}
