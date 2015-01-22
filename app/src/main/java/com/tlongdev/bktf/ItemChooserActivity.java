package com.tlongdev.bktf;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

//TODO implement item chooser for calculator
public class ItemChooserActivity extends Activity implements AdapterView.OnItemSelectedListener,
        TextWatcher, LoaderManager.LoaderCallbacks<Cursor> {

    private LinearLayout rootView;

    private Spinner itemTypeSpinner;
    private EditText searchInput;
    private ListView itemChooserList;
    private Spinner effectTypeSpinner;
    private Button cancelButton;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_chooser);
        setFinishOnTouchOutside(false);
        rootView = (LinearLayout)getWindow().getDecorView().findViewById(R.id.list_view);

        searchInput = (EditText)findViewById(R.id.edit_text_search);
        searchInput.addTextChangedListener(this);

        itemChooserList = (ListView)findViewById(R.id.list_view_item_chooser);

        effectTypeSpinner = (Spinner)findViewById(R.id.spinner_effect);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> effectsAdapter = ArrayAdapter.createFromResource(this,
                R.array.effects, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        effectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        effectTypeSpinner.setAdapter(effectsAdapter);

        itemTypeSpinner = (Spinner)findViewById(R.id.spinner_item);
        itemTypeSpinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.item_chooser_spinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        itemTypeSpinner.setAdapter(typeAdapter);


        cancelButton = (Button)findViewById(R.id.button_cancel);
        addButton = (Button)findViewById(R.id.button_add);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                effectTypeSpinner.setVisibility(View.GONE);
                break;
            case 1:
                effectTypeSpinner.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
