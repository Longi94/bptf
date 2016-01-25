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

package com.tlongdev.bktf.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.R;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTouch;

/**
 * Converter fragment. Let's the user quickly convert between currencies.
 */
public class ConverterFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = ConverterFragment.class.getSimpleName();

    /**
     * The {@link Tracker} used to record screen views.
     */
    private Tracker mTracker;

    /**
     * Inputs
     */
    @Bind(R.id.edit_text_earbuds) EditText inputEarbuds;
    @Bind(R.id.edit_text_keys) EditText inputKeys;
    @Bind(R.id.edit_text_metal) EditText inputMetal;
    @Bind(R.id.edit_text_usd) EditText inputUsd;

    /**
     * the view that is currently in focus
     */
    private EditText focus;

    /**
     * Constructor.
     */
    public ConverterFragment() {
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

        // Obtain the shared Tracker instance.
        BptfApplication application = (BptfApplication) (getActivity()).getApplication();
        mTracker = application.getDefaultTracker();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_converter, container, false);

        ButterKnife.bind(this, rootView);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        //Whenever a text of a input changes, update the value of the other three
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
                            Price price = new Price(Double.parseDouble(s.toString()), Currency.BUD);
                            inputKeys.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.KEY, false))));
                            inputMetal.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.METAL, false))));
                            inputUsd.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.USD, false))));
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();

                    ((BptfApplication)getActivity().getApplication()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                            .setDescription("Converter exception:ConverterFragment, Message: " + t.getMessage())
                            .setFatal(false)
                            .build());
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
                            Price price = new Price(Double.parseDouble(s.toString()), Currency.KEY);
                            inputEarbuds.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.BUD, false))));
                            inputMetal.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.METAL, false))));
                            inputUsd.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.USD, false))));
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();

                    ((BptfApplication)getActivity().getApplication()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                            .setDescription("Converter exception:ConverterFragment, Message: " + t.getMessage())
                            .setFatal(false)
                            .build());
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
                            Price price = new Price(Double.parseDouble(s.toString()), Currency.METAL);
                            inputKeys.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.KEY, false))));
                            inputEarbuds.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.BUD, false))));
                            inputUsd.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.USD, false))));
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();

                    ((BptfApplication)getActivity().getApplication()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                            .setDescription("Converter exception:ConverterFragment, Message: " + t.getMessage())
                            .setFatal(false)
                            .build());
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
                            Price price = new Price(Double.parseDouble(s.toString()), Currency.USD);
                            inputKeys.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.KEY, false))));
                            inputMetal.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.METAL, false))));
                            inputEarbuds.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.BUD, false))));
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();

                    ((BptfApplication)getActivity().getApplication()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                            .setDescription("Converter exception:ConverterFragment, Message: " + t.getMessage())
                            .setFatal(false)
                            .build());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Set the initial values
        Price price = new Price(1, Currency.BUD);
        inputEarbuds.setText("1");
        inputEarbuds.setSelection(inputEarbuds.length());
        try {
            inputKeys.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.KEY, false))));
            inputMetal.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.METAL, false))));
            inputUsd.setText(String.valueOf(Utility.formatDouble(price.getConvertedPrice(getActivity(), Currency.USD, false))));
        } catch (Throwable t) {
            t.printStackTrace();

            ((BptfApplication)getActivity().getApplication()).getDefaultTracker().send(new HitBuilders.ExceptionBuilder()
                    .setDescription("Converter exception:ConverterFragment, Message: " + t.getMessage())
                    .setFatal(false)
                    .build());
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName("Converter");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_converter, menu);
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
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    @OnClick({R.id.calculator_0, R.id.calculator_1, R.id.calculator_2, R.id.calculator_3,
            R.id.calculator_4, R.id.calculator_5, R.id.calculator_6, R.id.calculator_7,
            R.id.calculator_8, R.id.calculator_9, R.id.calculator_dot, R.id.calculator_delete})
    public void onClick(View v) {
        //Handle all the clicks
        String s = null;
        switch (v.getId()) {
            case R.id.calculator_0:
                s = "0";
                break;
            case R.id.calculator_1:
                s = "1";
                break;
            case R.id.calculator_2:
                s = "2";
                break;
            case R.id.calculator_3:
                s = "3";
                break;
            case R.id.calculator_4:
                s = "4";
                break;
            case R.id.calculator_5:
                s = "5";
                break;
            case R.id.calculator_6:
                s = "6";
                break;
            case R.id.calculator_7:
                s = "7";
                break;
            case R.id.calculator_8:
                s = "8";
                break;
            case R.id.calculator_9:
                s = "9";
                break;
            case R.id.calculator_dot:
                s = ".";
                break;
            case R.id.calculator_delete:
                break;
        }

        //This magical code simulates the behavior of a normal soft keyboard edit
        //Also properly handles text selections
        String prev = focus.getText().toString();
        int selectionStart = focus.getSelectionStart();
        int selectionEnd = focus.getSelectionEnd();
        if (s != null) {
            if (!s.contains(".") || !prev.contains(".")) {
                focus.setText(prev.substring(0, selectionStart) + s +
                        prev.substring(selectionEnd, prev.length()));
                focus.setSelection(selectionStart + 1);
            }
        } else if (prev.length() > 0) {
            if (selectionStart != selectionEnd) {
                focus.setText(prev.substring(0, selectionStart) +
                        prev.substring(selectionEnd, prev.length()));
                focus.setSelection(selectionStart);
            } else {
                focus.setText(prev.substring(0, selectionStart - 1) +
                        prev.substring(selectionEnd, prev.length()));
                focus.setSelection(selectionStart - 1);
            }
        }
    }

    @OnFocusChange({R.id.edit_text_earbuds, R.id.edit_text_keys, R.id.edit_text_metal,
            R.id.edit_text_usd})
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.edit_text_earbuds:
                    focus = inputEarbuds;
                    break;
                case R.id.edit_text_keys:
                    focus = inputKeys;
                    break;
                case R.id.edit_text_metal:
                    focus = inputMetal;
                    break;
                case R.id.edit_text_usd:
                    focus = inputUsd;
                    break;
            }
        }
    }

    /**
     * This disables the soft keyboard because we don't need it
     */
    @OnTouch({R.id.edit_text_earbuds, R.id.edit_text_keys, R.id.edit_text_metal,
            R.id.edit_text_usd})
    public boolean onTouch(View v, MotionEvent event){
        v.onTouchEvent(event);
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        return true;
    }
}
