package com.tlongdev.bktf;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.tlongdev.bktf.adapter.ItemChooserAdapter;
import com.tlongdev.bktf.data.PriceListContract;

public class ItemChooserActivity extends FragmentActivity implements
        TextWatcher, LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, View.OnFocusChangeListener {

    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_ITEM_COUNT = "item_count";

    private Spinner itemTypeSpinner;
    private EditText searchInput;
    private EditText numberInput;
    private Spinner effectTypeSpinner;
    private Button addButton;
    private TextView noMatchTextView;

    private ItemChooserAdapter adapter;

    //The columns we need
    private static final String[] PRICE_LIST_COLUMNS = {
            PriceListContract.PriceEntry.TABLE_NAME + "." + PriceListContract.PriceEntry._ID,
            PriceListContract.PriceEntry.COLUMN_ITEM_NAME,
            PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY,
            PriceListContract.PriceEntry.COLUMN_ITEM_TRADABLE,
            PriceListContract.PriceEntry.COLUMN_ITEM_CRAFTABLE,
            PriceListContract.PriceEntry.COLUMN_PRICE_INDEX
    };

    //Indexes of the columns above
    public static final int COL_PRICE_LIST_ID = 0;
    public static final int COL_PRICE_LIST_NAME = 1;
    public static final int COL_PRICE_LIST_QUAL = 2;
    public static final int COL_PRICE_LIST_TRAD = 3;
    public static final int COL_PRICE_LIST_CRAF = 4;
    public static final int COL_PRICE_LIST_INDE = 5;

    //Selection
    private static final String sGeneralSearch =
            PriceListContract.PriceEntry.TABLE_NAME+
                    "." + PriceListContract.PriceEntry.COLUMN_ITEM_NAME + " LIKE ? AND NOT(" +
                    PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " = 5 AND " +
                    PriceListContract.PriceEntry.COLUMN_PRICE_INDEX + " != 0)";

    private static final String sUnusualSearch =
            PriceListContract.PriceEntry.TABLE_NAME+
                    "." + PriceListContract.PriceEntry.COLUMN_ITEM_NAME + " LIKE ? AND " +
                    PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " = 5 AND " +
                    PriceListContract.PriceEntry.COLUMN_PRICE_INDEX + " = ?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_chooser);
        setFinishOnTouchOutside(false);

        noMatchTextView = (TextView)findViewById(R.id.text_view_no_match);

        searchInput = (EditText)findViewById(R.id.edit_text_search);
        searchInput.addTextChangedListener(this);

        numberInput = (EditText)findViewById(R.id.edit_text_count);
        numberInput.setOnFocusChangeListener(this);

        ListView itemChooserList = (ListView) findViewById(R.id.list_view_item_chooser);
        adapter = new ItemChooserAdapter(this, null, 0);
        itemChooserList.setAdapter(adapter);
        itemChooserList.setOnItemClickListener(this);

        effectTypeSpinner = (Spinner)findViewById(R.id.spinner_effect);
        effectTypeSpinner.setEnabled(false);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> effectsAdapter = ArrayAdapter.createFromResource(this,
                R.array.effects, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        effectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        effectTypeSpinner.setAdapter(effectsAdapter);
        effectTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private int previousSelection = -1;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (previousSelection != position) {
                    previousSelection = position;
                    getSupportLoaderManager().restartLoader(0, null, ItemChooserActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        itemTypeSpinner = (Spinner)findViewById(R.id.spinner_item);
        itemTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            private int previousSelection = -1;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (previousSelection != position){
                    switch (position) {
                        case 0:
                            effectTypeSpinner.setClickable(false);
                            effectTypeSpinner.setEnabled(false);
                            break;
                        case 1:
                            effectTypeSpinner.setClickable(true);
                            effectTypeSpinner.setEnabled(true);
                            break;
                    }
                    getSupportLoaderManager().restartLoader(0, null, ItemChooserActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.item_chooser_spinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        itemTypeSpinner.setAdapter(typeAdapter);


        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        addButton = (Button)findViewById(R.id.button_add);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_ITEM_ID, adapter.getItemId());
                resultIntent.putExtra(EXTRA_ITEM_COUNT, Integer.parseInt(numberInput.getText().toString()));
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        if (itemTypeSpinner.getSelectedItemPosition() == 0) {
            selection = sGeneralSearch;
        } else {
            selection = sUnusualSearch;
        }
        String[] selectionArgs;
        String query = searchInput.getText().toString();
        if (query.equals("")){
            selectionArgs = new String[]{""};
        } else {
            if (itemTypeSpinner.getSelectedItemPosition() == 0) {
                selectionArgs = new String[]{"%" + query + "%"};
            } else {
                int[] indexes = getResources().getIntArray(R.array.effects_indexes);
                selectionArgs = new String[]{"%" + query + "%", "" + indexes[effectTypeSpinner.getSelectedItemPosition()]};
            }
        }
        return new CursorLoader(
                this,
                PriceListContract.PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                selection,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        adapter.setSelectedIndex(-1);
        adapter.notifyDataSetChanged();
        addButton.setEnabled(false);
        if (data.getCount() == 0){
            noMatchTextView.setVisibility(View.VISIBLE);
        } else {
            noMatchTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.setSelectedIndex(position);
        adapter.notifyDataSetChanged();
        addButton.setEnabled(true);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus && numberInput.getText().toString().equals("") ||
                Integer.parseInt(numberInput.getText().toString()) < 0){
            numberInput.setText("0");
        }
    }
}
