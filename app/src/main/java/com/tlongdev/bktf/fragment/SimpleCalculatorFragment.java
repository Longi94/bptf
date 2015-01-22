package com.tlongdev.bktf.fragment;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

public class SimpleCalculatorFragment extends Fragment {

    private EditText inputEarbuds;
    private EditText inputKeys;
    private EditText inputMetal;
    private EditText inputUsd;

    public SimpleCalculatorFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_simple_calculator, container, false);
        inputEarbuds = (EditText)rootView.findViewById(R.id.edit_text_earbuds);
        inputKeys = (EditText)rootView.findViewById(R.id.edit_text_keys);
        inputMetal = (EditText)rootView.findViewById(R.id.edit_text_metal);
        inputUsd = (EditText)rootView.findViewById(R.id.edit_text_usd);

        inputEarbuds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (inputEarbuds.isFocused()) {
                        if (s.toString().equals("")){
                            inputKeys.setText(null);
                            inputMetal.setText(null);
                            inputUsd.setText(null);
                        } else {
                            inputKeys.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_BUD, Utility.CURRENCY_KEY), 2)));
                            inputMetal.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_BUD, Utility.CURRENCY_METAL), 2)));
                            inputUsd.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_BUD, Utility.CURRENCY_USD), 2)));
                        }
                    }
                } catch (Throwable throwable) {
                    if (Utility.isDebugging(getActivity()))
                        throwable.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        inputKeys.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (inputKeys.isFocused()) {
                        if (s.toString().equals("")){
                            inputEarbuds.setText(null);
                            inputMetal.setText(null);
                            inputUsd.setText(null);
                        } else {
                            inputEarbuds.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_KEY, Utility.CURRENCY_BUD), 2)));
                            inputMetal.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_KEY, Utility.CURRENCY_METAL), 2)));
                            inputUsd.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_KEY, Utility.CURRENCY_USD), 2)));
                        }
                    }
                } catch (Throwable throwable) {
                    if (Utility.isDebugging(getActivity()))
                        throwable.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        inputMetal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (inputMetal.isFocused()) {
                        if (s.toString().equals("")){
                            inputKeys.setText(null);
                            inputEarbuds.setText(null);
                            inputUsd.setText(null);
                        } else {
                            inputKeys.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_METAL, Utility.CURRENCY_KEY), 2)));
                            inputEarbuds.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_METAL, Utility.CURRENCY_BUD), 2)));
                            inputUsd.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_METAL, Utility.CURRENCY_USD), 2)));
                        }
                    }
                } catch (Throwable throwable) {
                    if (Utility.isDebugging(getActivity()))
                        throwable.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        inputUsd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (inputUsd.isFocused()) {
                        if (s.toString().equals("")){
                            inputKeys.setText(null);
                            inputMetal.setText(null);
                            inputEarbuds.setText(null);
                        } else {
                            inputKeys.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_USD, Utility.CURRENCY_KEY), 2)));
                            inputMetal.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_USD, Utility.CURRENCY_METAL), 2)));
                            inputEarbuds.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                                    Double.parseDouble(s.toString()), Utility.CURRENCY_USD, Utility.CURRENCY_BUD), 2)));
                        }
                    }
                } catch (Throwable throwable) {
                    if (Utility.isDebugging(getActivity()))
                        throwable.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        inputEarbuds.setText("1");
        try {
            inputKeys.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                    1, Utility.CURRENCY_BUD, Utility.CURRENCY_KEY), 2)));
            inputMetal.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                    1, Utility.CURRENCY_BUD, Utility.CURRENCY_METAL), 2)));
            inputUsd.setText(String.valueOf(Utility.roundDouble(Utility.convertPrice(getActivity(),
                    1, Utility.CURRENCY_BUD, Utility.CURRENCY_USD), 2)));
        } catch (Throwable throwable) {
            if (Utility.isDebugging(getActivity()))
                throwable.printStackTrace();
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_simple_calculator, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.action_show_advanced:
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putBoolean(getString(R.string.pref_prefered_advanced_calculator), true).apply();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.simple_fade_in, R.anim.simple_fade_out)
                        .replace(R.id.container, new AdvancedCalculatorFragment())
                        .commit();
                break;
        }
        return true;
    }
}
