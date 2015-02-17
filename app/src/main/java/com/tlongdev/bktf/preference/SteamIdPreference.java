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

public class SteamIdPreference extends DialogPreference {

    private EditText steamIdInput;

    public SteamIdPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setDialogLayoutResource(R.layout.preference_dialog_steam_id);
        setSummary(PreferenceManager.getDefaultSharedPreferences(context).getString("Steam ID", ""));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            getEditor().putString("Steam ID", steamIdInput.getText().toString()).apply();
            setSummary(steamIdInput.getText().toString());
        }
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        steamIdInput = (EditText)view.findViewById(R.id.edit_text_steam_id);
        steamIdInput.setText(getSharedPreferences().getString("Steam ID", ""));

        ((TextView)view.findViewById(R.id.steam_id_64_bit)).setText(
                Html.fromHtml("steamcommunity.com/profiles/<b>id</b>")/*7656119XXXXXXXXXX*/
        );
        ((TextView)view.findViewById(R.id.steam_id_vanity)).setText(
                Html.fromHtml("steamcommunity.com/id/<b>name</b>/")
        );
    }
}
