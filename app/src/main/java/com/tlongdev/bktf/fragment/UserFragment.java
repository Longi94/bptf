package com.tlongdev.bktf.fragment;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tlongdev.bktf.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserFragment extends Fragment {

    private TextView playerName;
    private TextView playerReputation;
    private TextView playerBackpackValue;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        playerName = (TextView)rootView.findViewById(R.id.text_view_player_name);
        playerReputation = (TextView)rootView.findViewById(R.id.text_view_player_reputation);
        playerBackpackValue = (TextView)rootView.findViewById(R.id.text_view_player_bp_value);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        playerName.setText(prefs.getString(getString(R.string.pref_player_name), ""));
        playerReputation.setText("Reputation: " + prefs.getInt(getString(R.string.pref_player_reputation), 0));
        playerBackpackValue.setText("Backpack value: " + prefs.getFloat(getString(R.string.pref_player_backpack_value_tf2), 0));

        new FetchUserInfo().execute();
        return rootView;
    }

    private class FetchUserInfo extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String userJsonStr = null;

            try {
                final String USER_INFO_BASE_URL = "http://backpack.tf/api/IGetUsers/v3/";
                final String KEY_STEAM_ID = "steamids";
                final String KEY_COMPRESS = "compress";

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

                Uri uri = Uri.parse(USER_INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_STEAM_ID, prefs.getString(getString(R.string.pref_steam_id), ""))
                        .appendQueryParameter(KEY_COMPRESS, "1")
                        .build();

                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                userJsonStr = buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("UserFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                parseUserInfoJson(userJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if(isAdded()){
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

                playerName.setText(prefs.getString(getString(R.string.pref_player_name), ""));
                playerReputation.setText("Reputation: " + prefs.getInt(getString(R.string.pref_player_reputation), 0));
                playerBackpackValue.setText("Backpack value: " + prefs.getFloat(getString(R.string.pref_player_backpack_value_tf2), 0));
            }
        }

        private void parseUserInfoJson(String jsonString) throws JSONException {

            final String OWM_RESPONSE = "response";
            final String OWM_SUCCESS = "success";
            final String OWM_CURRENT_TIME = "current_time";
            final String OWM_PLAYERS = "players";
            final String OWM_BACKPACK_VALUE = "backpack_value";
            final String OWM_BACKPACK_VALUE_TF2 = "440";
            final String OWM_BACKPACK_VALUE_DOTA2 = "570";
            final String OWM_BACKPACK_UPDATE = "backpack_update";
            final String OWM_PLAYER_NAME = "name";
            final String OWM_PLAYER_REPUTATION = "backpack_tf_reputation";
            final String OWM_PLAYER_GROUP = "backpack_tf_group";
            final String OWM_PLAYER_BANNED = "backpack_tf_banned";
            final String OWM_PLAYER_TRUST = "backpack_tf_trust";
            final String OWM_PLAYER_TRUST_FOR = "for";
            final String OWM_PLAYER_TRUST_AGAINST = "against";
            final String OWM_PLAYER_SCAMMER = "steamrep_scammer";
            final String OWM_PLAYER_BAN_ECONOMY = "ban_economy";
            final String OWM_PLAYER_BAN_COMMUNITY = "ban_community";
            final String OWM_PLAYER_BAN_VAC = "ban_vac";
            final String OWM_PLAYER_NOTIFICATION = "notification";

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

            if (response.getInt(OWM_SUCCESS) == 0) {
                return;
            }

            JSONObject players = response.getJSONObject(OWM_PLAYERS);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            JSONObject current_user = players.getJSONObject(prefs.getString(getString(R.string.pref_steam_id), ""));

            if (current_user.getInt(OWM_SUCCESS) == 1) {

                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(getString(R.string.pref_player_name), current_user.getString(OWM_PLAYER_NAME));
                editor.putInt(getString(R.string.pref_player_reputation), current_user.getInt(OWM_PLAYER_REPUTATION));
                editor.putFloat(getString(R.string.pref_player_backpack_value_tf2),
                        (float) current_user.getJSONObject(OWM_BACKPACK_VALUE).getDouble(OWM_BACKPACK_VALUE_TF2));
                editor.putFloat(getString(R.string.pref_player_backpack_value_dota2),
                        (float)current_user.getJSONObject(OWM_BACKPACK_VALUE).getDouble(OWM_BACKPACK_VALUE_DOTA2));

                if (current_user.has(OWM_PLAYER_GROUP)) {
                    editor.putBoolean(getString(R.string.pref_player_group), true);
                }
                if (current_user.has(OWM_PLAYER_BANNED)) {
                    editor.putBoolean(getString(R.string.pref_player_banned), true);
                }
                if (current_user.has(OWM_PLAYER_SCAMMER)) {
                    editor.putBoolean(getString(R.string.pref_player_scammer), true);
                }
                if (current_user.has(OWM_PLAYER_BAN_COMMUNITY)) {
                    editor.putBoolean(getString(R.string.pref_player_community_banned), true);
                }
                if (current_user.has(OWM_PLAYER_BAN_ECONOMY)) {
                    editor.putBoolean(getString(R.string.pref_player_economy_banned), true);
                }
                if (current_user.has(OWM_PLAYER_BAN_VAC)) {
                    editor.putBoolean(getString(R.string.pref_player_vac_banned), true);
                }
                if (current_user.has(OWM_PLAYER_TRUST)) {
                    editor.putInt(getString(R.string.pref_player_trust_positive), current_user.getInt(OWM_PLAYER_TRUST_FOR));
                    editor.putInt(getString(R.string.pref_player_trust_negative), current_user.getInt(OWM_PLAYER_TRUST_AGAINST));
                }

                editor.apply();
            }
        }
    }

}
