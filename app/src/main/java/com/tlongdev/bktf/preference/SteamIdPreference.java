package com.tlongdev.bktf.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tlongdev.bktf.R;

/**
 * Custom dialog preference for entering the steam id. Contains brief instructions on how to find
 * the user's steam id.
 */
public class SteamIdPreference extends DialogPreference {

    //The input field
    private EditText steamIdInput;

    //Context for getting resources
    private Context mContext;

    /**
     * Constructor
     *
     * @param context context
     * @param attrs   attributes
     */
    public SteamIdPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        //Value will be saved manually
        setPersistent(false);

        //The layout resource for the dialog
        setDialogLayoutResource(R.layout.preference_dialog_steam_id);

        //Set the summary to the saved value
        setSummary(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_steam_id), ""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            //Save the input value to the preferences
            getEditor().putString(mContext.getString(R.string.pref_steam_id),
                    steamIdInput.getText().toString()).apply();
            setSummary(steamIdInput.getText().toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        //The input field
        steamIdInput = (EditText) view.findViewById(R.id.edit_text_steam_id);
        steamIdInput.setText(getSharedPreferences()
                .getString(mContext.getString(R.string.pref_steam_id), ""));

        //Format the instructions.
        ((TextView) view.findViewById(R.id.steam_id_64_bit)).setText(
                Html.fromHtml("steamcommunity.com/profiles/<b>id</b>")/*7656119XXXXXXXXXX*/
        );
        ((TextView) view.findViewById(R.id.steam_id_vanity)).setText(
                Html.fromHtml("steamcommunity.com/id/<b>name</b>/")
        );
    }
}
