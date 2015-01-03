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
import com.tlongdev.bktf.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationsService extends Service {

    private static final String CHECK_FOR_NOTIFICATIONS = "notification";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.hasExtra("notification") &&
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_notification), false)) {
            new CheckNotificationTask(this).execute();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_notification), false)) {
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            long delay = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.pref_notification_interval), "86400000"));
            Intent intent = new Intent(this, NotificationsService.class);
            intent.putExtra(CHECK_FOR_NOTIFICATIONS, true);
            alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + delay,
                    PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            );
        }
    }

    private class CheckNotificationTask extends AsyncTask<Void, Void, Integer>{

        Context mContext;

        private CheckNotificationTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return checkForNotifications();
        }

        @Override
        protected void onPostExecute(Integer aInt) {
            if (aInt > 0) {
                Uri webPage = Uri.parse("http://backpack.tf/notifications");
                Intent i = new Intent(Intent.ACTION_VIEW, webPage);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(mContext)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("" + aInt + " unread notificaions on bp.tf")
                                .setContentText("Tap to open in browser.")
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, mBuilder.build());
            }

            stopSelf();
        }

        private int checkForNotifications() {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection;
            BufferedReader reader;

            // Will contain the raw JSON response as a string.
            String jsonString;

            try {
                final String USER_INFO_BASE_URL = "http://backpack.tf/api/IGetUsers/v3/";
                final String KEY_STEAM_ID = "steamids";
                final String KEY_COMPRESS = "compress";

                String steamId = Utility.getResolvedSteamId(mContext);
                if (steamId == null) {
                    return 0;
                }

                Uri uri = Uri.parse(USER_INFO_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_STEAM_ID, steamId)
                        .appendQueryParameter(KEY_COMPRESS, "1")
                        .build();

                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                String line;
                if (inputStream != null) {

                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    if (buffer.length() > 0) {
                        jsonString = buffer.toString();
                        return parseUserInfoJson(jsonString, steamId);
                    }
                }
            } catch (IOException | JSONException e) {
                Toast.makeText(mContext, "bptf: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return 0;
        }

        private int parseUserInfoJson(String jsonString, String steamId) throws JSONException {

            final String OWM_RESPONSE = "response";
            final String OWM_SUCCESS = "success";
            final String OWM_PLAYERS = "players";
            final String OWM_PLAYER_NOTIFICATION = "notification";

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

            if (response.getInt(OWM_SUCCESS) == 0) {
                return 0;
            }

            JSONObject players = response.getJSONObject(OWM_PLAYERS);

            JSONObject current_user = players.getJSONObject(steamId);

            if (current_user.getInt(OWM_SUCCESS) == 1) {
                return current_user.getInt(OWM_PLAYER_NOTIFICATION);
            }

            return 0;
        }
    }
}
