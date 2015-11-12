package com.tlongdev.bktf.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Profile;
import com.tlongdev.bktf.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Service for checking for notifications in the background.
 */
public class NotificationsService extends Service {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = NotificationsService.class.getSimpleName();

    //Intent extra keys for indicating the need for the notification check
    private static final String CHECK_FOR_NOTIFICATIONS = "notification";

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Only check for notifications if the service was started by scheduling itself
        if (intent.hasExtra(CHECK_FOR_NOTIFICATIONS) &&
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getString(R.string.pref_notification), false)
                && Utility.isNetworkAvailable(this)) {

            //Start checking for notifications
            new CheckNotificationTask(this).execute();
        }
        return START_NOT_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        //It's a started service, so this is unused
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        //When the service stops schedule the service to start after the set time
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_notification), false)) {
            //Schedule the service to run again after a given time
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

            //The amount of tiem to wait
            long delay = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.pref_notification_interval), "86400000"));

            //The intent that will be given to the service
            Intent intent = new Intent(this, NotificationsService.class);
            intent.putExtra(CHECK_FOR_NOTIFICATIONS, true);

            //Schedule
            alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + delay,
                    PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            );
        }
    }

    /**
     * Task the actually checks for notifications.
     */
    private class CheckNotificationTask extends AsyncTask<Void, Void, Integer> {

        //The context the task runs in
        private Context mContext;

        /**
         * Constructor
         *
         * @param context the context the task runs in
         */
        private CheckNotificationTask(Context context) {
            mContext = context;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Integer doInBackground(Void... params) {
            //Return the number of new notifications
            return checkForNotifications();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Integer aInt) {
            //Show notification if there is a new notifications on backpack.tf
            if (aInt > 0) {
                //The link to open when the user taps on the notification
                Uri webPage = Uri.parse("http://backpack.tf/notifications");

                //The intent to be used when the user taps on the notification
                Intent i = new Intent(Intent.ACTION_VIEW, webPage);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                //Build the notification
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(mContext)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(getString(R.string.notification_unread_title, aInt))
                                .setContentText(getString(R.string.notification_unread_description))
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                //Show the noitification
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, mBuilder.build());
            }

            //Stop the service
            stopSelf();
        }

        /**
         * Connect to the api and check for notifications.
         */
        private int checkForNotifications() {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection;
            BufferedReader reader;

            // Will contain the raw JSON response as a string.
            String jsonString;

            try {
                //The GetUsers api and input keys
                final String USER_INFO_BASE_URL = mContext.getString(R.string.backpack_tf_get_users);
                final String KEY_STEAM_ID = "steamids";
                final String KEY_COMPRESS = "compress";

                //Get the resolved steamid
                String steamId = Profile.getResolvedSteamId(mContext);

                if (steamId == null) {
                    //Thre is no resolved steamid, no ontifications
                    return 0;
                }

                //Build the URI
                Uri uri = Uri.parse(USER_INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_STEAM_ID, steamId)
                        .appendQueryParameter(KEY_COMPRESS, "1")
                        .build();

                //Initialize the URL
                URL url = new URL(uri.toString());

                //Open connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Get the input stream
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                // Nothing to do if the stream was empty.
                if (inputStream != null) {

                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    //Read the input
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    if (buffer.length() > 0) {
                        // If the stream was empty there is no point in parsing.
                        jsonString = buffer.toString();
                        //Get the number of new notifications
                        return getNotificationCount(jsonString, steamId);
                    }
                }
            } catch (IOException | JSONException e) {
                //There was an error, notify the user
                Toast.makeText(mContext, "bptf: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return 0;
        }

        /**
         * Get the number of new notification from the JSON.
         *
         * @param jsonString the JSON string to parse from
         * @param steamId    the steamid of the user
         * @return the number of new notifications
         * @throws JSONException
         */
        private int getNotificationCount(String jsonString, String steamId) throws JSONException {

            //All the JSON keys needed to parse
            final String OWM_RESPONSE = "response";
            final String OWM_SUCCESS = "success";
            final String OWM_PLAYERS = "players";
            final String OWM_PLAYER_NOTIFICATION = "notification";

            //Get the response JSON
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

            if (response.getInt(OWM_SUCCESS) == 0) {
                //Query unsuccessful
                return 0;
            }

            //Get the player JSON object
            JSONObject players = response.getJSONObject(OWM_PLAYERS);
            JSONObject current_user = players.getJSONObject(steamId);

            if (current_user.getInt(OWM_SUCCESS) == 1) {
                //Return the number of new notifications
                return current_user.getInt(OWM_PLAYER_NOTIFICATION);
            }

            return 0;
        }
    }
}
