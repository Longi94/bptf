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

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;
import com.tlongdev.bktf.activity.SearchActivity;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Price;

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
     * Inputs
     */
    private EditText inputEarbuds;
    private EditText inputKeys;
    private EditText inputMetal;
    private EditText inputUsd;

    /**
     * the view that is currently in focus
     */
    private EditText focus;

    /**
     * This disables the soft keyboard because we don't need it
     */
    private View.OnTouchListener inputListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.onTouchEvent(event);
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            return true;
        }
    };

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_converter, container, false);

        //Set the toolbar to the main activity's action bar
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));

        inputEarbuds = (EditText) rootView.findViewById(R.id.edit_text_earbuds);
        inputKeys = (EditText) rootView.findViewById(R.id.edit_text_keys);
        inputMetal = (EditText) rootView.findViewById(R.id.edit_text_metal);
        inputUsd = (EditText) rootView.findViewById(R.id.edit_text_usd);

        //Handle the touch events inside the fragment
        inputEarbuds.setOnTouchListener(inputListener);
        inputKeys.setOnTouchListener(inputListener);
        inputMetal.setOnTouchListener(inputListener);
        inputUsd.setOnTouchListener(inputListener);

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
                            inputKeys.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.KEY, false), 2)));
                            inputMetal.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.METAL, false), 2)));
                            inputUsd.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.USD, false), 2)));
                        }
                    }
                } catch (Throwable throwable) {
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
                            Price price = new Price(Double.parseDouble(s.toString()), Currency.KEY);
                            inputEarbuds.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.BUD, false), 2)));
                            inputMetal.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.METAL, false), 2)));
                            inputUsd.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.USD, false), 2)));
                        }
                    }
                } catch (Throwable throwable) {
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
                            Price price = new Price(Double.parseDouble(s.toString()), Currency.METAL);
                            inputKeys.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.KEY, false), 2)));
                            inputEarbuds.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.BUD, false), 2)));
                            inputUsd.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.USD, false), 2)));
                        }
                    }
                } catch (Throwable throwable) {
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
                            Price price = new Price(Double.parseDouble(s.toString()), Currency.USD);
                            inputKeys.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.KEY, false), 2)));
                            inputMetal.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.METAL, false), 2)));
                            inputEarbuds.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.BUD, false), 2)));
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
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
            inputKeys.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.KEY, false), 2)));
            inputMetal.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.METAL, false), 2)));
            inputUsd.setText(String.valueOf(Utility.roundDouble(price.getConvertedPrice(getActivity(), Currency.USD, false), 2)));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        inputEarbuds.setOnFocusChangeListener(this);
        inputKeys.setOnFocusChangeListener(this);
        inputMetal.setOnFocusChangeListener(this);
        inputUsd.setOnFocusChangeListener(this);

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
    @Override
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

    @Override
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
}
