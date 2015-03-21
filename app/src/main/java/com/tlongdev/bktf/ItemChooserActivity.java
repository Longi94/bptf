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

/**
 *
 */
public class ItemChooserActivity extends FragmentActivity implements
        TextWatcher, LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener,
        View.OnFocusChangeListener {

    //Extra keys. Only used when the user is editing the item.
    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_ITEM_COUNT = "item_count";

    //Indexes of the columns below
    public static final int COL_PRICE_LIST_ID = 0;
    public static final int COL_PRICE_LIST_NAME = 1;
    public static final int COL_PRICE_LIST_QUAL = 2;
    public static final int COL_PRICE_LIST_TRAD = 3;
    public static final int COL_PRICE_LIST_CRAF = 4;
    public static final int COL_PRICE_LIST_INDE = 5;

    //The columns we need
    private static final String[] PRICE_LIST_COLUMNS = {
            PriceListContract.PriceEntry.TABLE_NAME + "." + PriceListContract.PriceEntry._ID,
            PriceListContract.PriceEntry.COLUMN_ITEM_NAME,
            PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY,
            PriceListContract.PriceEntry.COLUMN_ITEM_TRADABLE,
            PriceListContract.PriceEntry.COLUMN_ITEM_CRAFTABLE,
            PriceListContract.PriceEntry.COLUMN_PRICE_INDEX
    };

    //Selection
    private static final String sGeneralSearch =
            PriceListContract.PriceEntry.TABLE_NAME +
                    "." + PriceListContract.PriceEntry.COLUMN_ITEM_NAME + " LIKE ? AND NOT(" +
                    PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " = 5 AND " +
                    PriceListContract.PriceEntry.COLUMN_PRICE_INDEX + " != 0)";
    private static final String sUnusualSearch =
            PriceListContract.PriceEntry.TABLE_NAME +
                    "." + PriceListContract.PriceEntry.COLUMN_ITEM_NAME + " LIKE ? AND " +
                    PriceListContract.PriceEntry.COLUMN_ITEM_QUALITY + " = 5 AND " +
                    PriceListContract.PriceEntry.COLUMN_PRICE_INDEX + " = ?";

    //References to all the views in the activity
    private Button addButton;
    private EditText numberInput;
    private EditText searchInput;
    private Spinner itemTypeSpinner;
    private Spinner effectTypeSpinner;
    private TextView noMatchTextView;

    //Adapter for the listview
    private ItemChooserAdapter adapter;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_chooser);

        //Prevent closing the activity by touching outside
        setFinishOnTouchOutside(false);

        //Find all the views
        noMatchTextView = (TextView) findViewById(R.id.text_view_no_match);
        searchInput = (EditText) findViewById(R.id.edit_text_search);
        numberInput = (EditText) findViewById(R.id.edit_text_count);
        effectTypeSpinner = (Spinner) findViewById(R.id.spinner_effect);
        itemTypeSpinner = (Spinner) findViewById(R.id.spinner_item);
        addButton = (Button) findViewById(R.id.button_add);

        //Listen for text changes
        searchInput.addTextChangedListener(this);

        //Listne for focus changes
        numberInput.setOnFocusChangeListener(this);

        //The listview that displays the serach result
        ListView itemChooserList = (ListView) findViewById(R.id.list_view_item_chooser);

        //Initialse he adapter and set it to the listview
        adapter = new ItemChooserAdapter(this, null, 0);
        itemChooserList.setAdapter(adapter);

        //Single choice listview
        itemChooserList.setOnItemClickListener(this);

        //Disaple the effect spinner by default
        effectTypeSpinner.setEnabled(false);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> effectsAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_effects, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        effectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        effectTypeSpinner.setAdapter(effectsAdapter);

        //Listen for selection in the spinner
        effectTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //Store the seleciton
            private int previousSelection = -1;

            /**
             * {@inheritDoc}
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Requery everytime the user changes the selection
                if (previousSelection != position) {
                    previousSelection = position;
                    getSupportLoaderManager().restartLoader(0, null, ItemChooserActivity.this);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Unusued
            }
        });

        //Listen for selection in the spinner
        itemTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //Store the seleciton
            private int previousSelection = -1;

            /**
             * {@inheritDoc}
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (previousSelection != position) {
                    switch (position) {
                        case 0:
                            //Disable the effect spinner when the user chooses normal items
                            effectTypeSpinner.setClickable(false);
                            effectTypeSpinner.setEnabled(false);
                            break;
                        case 1:
                            //Enable the effect spinner when the user chooses unusual items
                            effectTypeSpinner.setClickable(true);
                            effectTypeSpinner.setEnabled(true);
                            break;
                    }
                    //Requery the data
                    getSupportLoaderManager().restartLoader(0, null, ItemChooserActivity.this);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Unusued
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_item_chooser_spinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        itemTypeSpinner.setAdapter(typeAdapter);

        //Find the 'cancel' button and set a listener to it
        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Finish the activity with no result
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        //Set a listener to the 'add' button
        addButton.setOnClickListener(new View.OnClickListener() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                //Send the data back t the previous activity, finish this activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_ITEM_ID, adapter.getItemId());
                resultIntent.putExtra(EXTRA_ITEM_COUNT, Integer.parseInt(numberInput.getText().toString()));
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        //Start the loader
        getSupportLoaderManager().initLoader(0, null, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Unusued
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //Unusued
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterTextChanged(Editable s) {
        //Requery the data everytime the search input is changed
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Get the search input
        String query = searchInput.getText().toString();

        //The selection based on the first spinner
        String selection;
        if (itemTypeSpinner.getSelectedItemPosition() == 0) {
            selection = sGeneralSearch;
        } else {
            selection = sUnusualSearch;
        }

        //The selection arguments
        String[] selectionArgs;
        if (query.equals("")) {
            //Text field is empty
            selectionArgs = new String[]{""};
        } else {
            if (itemTypeSpinner.getSelectedItemPosition() == 0) {
                //Searching fro normal items
                selectionArgs = new String[]{"%" + query + "%"};
            } else {
                //Searching for unusual items, get the unusual index from the array resource
                int[] indexes = getResources().getIntArray(R.array.array_effects_indexes);
                selectionArgs = new String[]{"%" + query + "%", "" + indexes[effectTypeSpinner.getSelectedItemPosition()]};
            }
        }

        //Query
        return new CursorLoader(
                this,
                PriceListContract.PriceEntry.CONTENT_URI,
                PRICE_LIST_COLUMNS,
                selection,
                selectionArgs,
                null
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Pass the data to the cursor
        adapter.swapCursor(data);
        //None selected by default
        adapter.setSelectedIndex(-1);
        //Redraw
        adapter.notifyDataSetChanged();
        //Disable the 'add' button as no item is selected
        addButton.setEnabled(false);

        if (data.getCount() == 0) {
            //There is no match, tell the user
            noMatchTextView.setVisibility(View.VISIBLE);
        } else {
            //There are matches
            noMatchTextView.setVisibility(View.GONE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Remove the data from the adapter
        adapter.swapCursor(null);
        //None selected by default
        adapter.setSelectedIndex(-1);
        //Redraw
        adapter.notifyDataSetChanged();
        //Disable the 'add' button as no item is selected
        addButton.setEnabled(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Set the selected item's index so it will be highlighted
        adapter.setSelectedIndex(position);
        //Redraw
        adapter.notifyDataSetChanged();
        //Enable the 'add' button
        addButton.setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        //When the count input looses focus and there is nothing entered, put 0 into it.
        if (!hasFocus && numberInput.getText().toString().equals("") ||
                Integer.parseInt(numberInput.getText().toString()) < 0) {
            numberInput.setText("0");
        }
    }
}
