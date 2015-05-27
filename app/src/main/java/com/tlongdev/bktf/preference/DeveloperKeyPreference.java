package com.tlongdev.bktf.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.tlongdev.bktf.R;

/**
 * Custom dialog preference for entering the the users own developer key. Contains brief instructions on how to find
 * the user's steam id.
 */
public class DeveloperKeyPreference extends DialogPreference {

    //The input field
    private EditText keyInput;

    //Context for getting resources
    private Context mContext;

    public DeveloperKeyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        //Value will be saved manually
        setPersistent(false);

        //The layout resource for the dialog
        setDialogLayoutResource(R.layout.preference_dialog_developer_key);

        //Set the summary depending on the saved value
        String original = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_developer_key), "");
        if (original == null || original.equals("")) {
            setSummary("Using the default key");
        } else {
            setSummary("Using a custom key");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        //The input field
        keyInput = (EditText) view.findViewById(R.id.edit_text_dev_key);
        keyInput.setText(getSharedPreferences().getString(mContext.getString(R.string.pref_developer_key), ""));

        view.findViewById(R.id.button_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create an URI for the intent.
                Uri webPage = Uri.parse("http://backpack.tf/api/register");

                //Open the link using the device default web browser.
                Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(intent);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        String newKey = keyInput.getText().toString();
        if (positiveResult) {
            //Save the input value to the preferences
            getEditor().putString(mContext.getString(R.string.pref_developer_key), newKey).apply();
        }

        //Set the summary depending on the new value
        if (newKey.equals("")) {
            setSummary("Using the default key");
        } else {
            setSummary("Using a custom key");
        }
    }
}
