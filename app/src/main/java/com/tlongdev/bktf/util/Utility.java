
/**
 * Copyright 2015 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.customtabs.CustomTabActivityHelper;
import com.tlongdev.bktf.customtabs.WebViewFallback;
import com.tlongdev.bktf.data.DatabaseContract.CalculatorEntry;
import com.tlongdev.bktf.data.DatabaseContract.FavoritesEntry;
import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Item;
import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.ui.activity.MainActivity;
import com.tlongdev.bktf.ui.activity.PriceHistoryActivity;
import com.tlongdev.bktf.widget.FavoritesWidget;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class (static only).
 */
public class Utility {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static final double EPSILON = 0.0001;

    /**
     * Check if the given steamId is a 64bit steamId using Regex.
     *
     * @param id steamId to examine
     * @return true if the steamId is actually a steamId
     */
    public static boolean isSteamId(String id) {
        //Every steamId looks like this: 7656119XXXXXXXXX
        return id != null && id.matches("7656119[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]");
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    /**
     * Format the unix timestamp the a user readable string.
     *
     * @param unixSeconds unix timestamp to be formatted
     * @return formatted string
     */
    public static String formatUnixTimeStamp(long unixSeconds) {
        Date date = new Date(unixSeconds * 1000L); // *1000 is to convert seconds to milliseconds
        //European format
        return sdf.format(date);
    }

    /**
     * Format the timestamp to a user friendly string that is the same as on steam profile pages.
     *
     * @param time timestamp to be formatted
     * @return formatted string
     */
    public static String formatLastOnlineTime(Context context, long time) {
        //If the time is longer than 2 days tho format is X days ago.
        if (time >= 172800000L) {
            long days = time / 86400000;
            return context.getString(R.string.time_passed_day_plural, days);
        }
        //If the time is longer than an hour, the format is X hour(s) Y minute(s) ago.
        if (time >= 3600000L) {
            long hours = time / 3600000;
            if (time % 3600000L == 0) {
                if (hours == 1)
                    return context.getString(R.string.time_passed_hour, hours);
                else {
                    return context.getString(R.string.time_passed_hour_plural, hours);
                }
            } else {
                long minutes = (time % 3600000L) / 60000;
                if (hours == 1)
                    if (minutes == 1)
                        return context.getString(R.string.time_measure_hour_minute, hours, minutes);
                    else
                        return context.getString(R.string.time_measure_hour_minute_p, hours, minutes);
                else {
                    if (minutes == 1)
                        return context.getString(R.string.time_measure_hour_p_minute, hours, minutes);
                    else
                        return context.getString(R.string.time_measure_hour_p_minute_p, hours, minutes);
                }
            }
        }
        //Else it was less than an hour ago, the format is X minute(s) ago.
        else {
            long minutes = time / 60000;
            if (minutes == 0) {
                return context.getString(R.string.time_measure_just_now);
            } else if (minutes == 1) {
                return context.getString(R.string.time_passed_minute, 1);
            } else {
                return context.getString(R.string.time_passed_minute_plural, minutes);
            }
        }
    }

    /**
     * Check whether the user if connected to the internet.
     *
     * @param context context for accessing system service
     * @return true if the user is connected to the internet
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Convenient method for storing double values in shared preferences.
     *
     * @param edit  shared preferences editor
     * @param key   preference key
     * @param value preference value
     * @return shared preference editor
     */
    public static SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit,
                                                     final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    /**
     * Convenient method for getting double values from shared preferences.
     *
     * @param prefs        shared preferences
     * @param key          preference key
     * @param defaultValue default preference value
     * @return the stored double value
     */
    public static double getDouble(final SharedPreferences prefs, final String key,
                                   final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    private static DecimalFormat df = new DecimalFormat("#.##");

    /**
     * Format floating point numbers to 2 decimal places
     *
     * @param value the number to format
     * @return the formatted string
     */
    public static String formatDouble(double value) {
        return df.format(value);
    }


    /**
     * Calculate the total raw metal.
     *
     * @param rawRef    number of raw refined metal
     * @param rawRec    number of raw reclaimed metal
     * @param rawScraps number of raw scrap metal
     * @return sum of raw metal in refined
     */
    public static double getRawMetal(int rawRef, int rawRec, int rawScraps) {
        return (1.0 / 9.0 * rawScraps) + (1.0 / 3.0 * rawRec) + rawRef;
    }

    /**
     * Return the complex query string for querying the raw price
     *
     * @param context context for converting prices
     * @return the query string
     */
    public static String getRawPriceQueryString(Context context) {
        Price key = new Price();
        key.setValue(1);
        key.setCurrency(Currency.KEY);
        Price usd = new Price();
        usd.setValue(1);
        usd.setCurrency(Currency.USD);
        Price bud = new Price();
        bud.setValue(1);
        bud.setCurrency(Currency.BUD);

        double keyMultiplier = key.getConvertedPrice(context, Currency.METAL, false);
        double usdMultiplier = usd.getConvertedPrice(context, Currency.METAL, false);
        double budMultiplier = bud.getConvertedPrice(context, Currency.METAL, false);

        return " CASE WHEN max IS NULL THEN ( " +
                " CASE WHEN currency = 'keys' THEN ( " +
                "price * " + keyMultiplier +
                " ) WHEN currency = 'earbuds' THEN ( " +
                "price * " + budMultiplier +
                " ) WHEN currency = 'usd' THEN ( " +
                "price * " + usdMultiplier +
                " ) WHEN currency = 'hat' THEN ( " +
                "price * 1.22 " +
                " ) ELSE ( " +
                "price" +
                " ) END " +
                " ) ELSE (" +
                " CASE WHEN currency = 'keys' THEN ( " +
                " ( price + max) / 2 * " + keyMultiplier +
                " ) WHEN currency = 'earbuds' THEN ( " +
                " ( price + max) / 2 * " + budMultiplier +
                " ) WHEN currency = 'usd' THEN ( " +
                " ( price + max) / 2 * " + usdMultiplier +
                " ) WHEN currency = 'hat' THEN ( " +
                " ( price + max) / 2 * 1.22 " +
                " ) ELSE ( " +
                " ( price + max) / 2 " +
                " ) END " +
                " ) END ";
    }

    public static void addToFavorites(Context context, Item item) {

        ContentValues cv = new ContentValues();

        cv.put(FavoritesEntry.COLUMN_DEFINDEX, item.getDefindex());
        cv.put(FavoritesEntry.COLUMN_ITEM_QUALITY, item.getQuality());
        cv.put(FavoritesEntry.COLUMN_ITEM_TRADABLE, item.isTradable() ? 1 : 0);
        cv.put(FavoritesEntry.COLUMN_ITEM_CRAFTABLE, item.isCraftable() ? 1 : 0);
        cv.put(FavoritesEntry.COLUMN_PRICE_INDEX, item.getPriceIndex());
        cv.put(FavoritesEntry.COLUMN_AUSTRALIUM, item.isAustralium() ? 1 : 0);
        cv.put(FavoritesEntry.COLUMN_WEAPON_WEAR, item.getWeaponWear());

        context.getContentResolver().insert(FavoritesEntry.CONTENT_URI, cv);

        notifyPricesWidgets(context);
    }

    public static void removeFromFavorites(Context context, Item item) {
        context.getContentResolver().delete(FavoritesEntry.CONTENT_URI,
                FavoritesEntry.COLUMN_DEFINDEX + " = ? AND " +
                        FavoritesEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        FavoritesEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                        FavoritesEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                        FavoritesEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                        FavoritesEntry.COLUMN_AUSTRALIUM + " = ? AND " +
                        FavoritesEntry.COLUMN_WEAPON_WEAR + " = ?",
                new String[]{String.valueOf(item.getDefindex()),
                        String.valueOf(item.getQuality()),
                        item.isTradable() ? "1" : "0",
                        item.isCraftable() ? "1" : "0",
                        String.valueOf(item.getPriceIndex()),
                        item.isAustralium() ? "1" : "0",
                        String.valueOf(item.getWeaponWear())
                });

        notifyPricesWidgets(context);
    }

    public static boolean isFavorite(Context context, Item item) {
        Cursor cursor = context.getContentResolver().query(
                FavoritesEntry.CONTENT_URI,
                null,
                FavoritesEntry.COLUMN_DEFINDEX + " = ? AND " +
                        FavoritesEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        FavoritesEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                        FavoritesEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                        FavoritesEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                        FavoritesEntry.COLUMN_AUSTRALIUM + " = ? AND " +
                        FavoritesEntry.COLUMN_WEAPON_WEAR + " = ?",
                new String[]{String.valueOf(item.getDefindex()),
                        String.valueOf(item.getQuality()),
                        item.isTradable() ? "1" : "0",
                        item.isCraftable() ? "1" : "0",
                        String.valueOf(item.getPriceIndex()),
                        item.isAustralium() ? "1" : "0",
                        String.valueOf(item.getWeaponWear())
                },
                null
        );

        boolean result = false;

        if (cursor != null) {
            result = cursor.getCount() > 0;
            cursor.close();
        }

        return result;
    }

    public static boolean isInCalculator(Context context, Item item) {
        Cursor cursor = context.getContentResolver().query(
                CalculatorEntry.CONTENT_URI,
                null,
                CalculatorEntry.COLUMN_DEFINDEX + " = ? AND " +
                        CalculatorEntry.COLUMN_ITEM_QUALITY + " = ? AND " +
                        CalculatorEntry.COLUMN_ITEM_TRADABLE + " = ? AND " +
                        CalculatorEntry.COLUMN_ITEM_CRAFTABLE + " = ? AND " +
                        CalculatorEntry.COLUMN_PRICE_INDEX + " = ? AND " +
                        CalculatorEntry.COLUMN_AUSTRALIUM + " = ? AND " +
                        CalculatorEntry.COLUMN_WEAPON_WEAR + " = ?",
                new String[]{String.valueOf(item.getDefindex()),
                        String.valueOf(item.getQuality()),
                        item.isTradable() ? "1" : "0",
                        item.isCraftable() ? "1" : "0",
                        String.valueOf(item.getPriceIndex()),
                        item.isAustralium() ? "1" : "0",
                        String.valueOf(item.getWeaponWear())
                },
                null
        );

        boolean result = false;

        if (cursor != null) {
            result = cursor.getCount() > 0;
            cursor.close();
        }

        return result;
    }

    public static void addToCalculator(Context context, Item item) {
        ContentValues cv = new ContentValues();

        cv.put(CalculatorEntry.COLUMN_DEFINDEX, item.getDefindex());
        cv.put(CalculatorEntry.COLUMN_ITEM_QUALITY, item.getQuality());
        cv.put(CalculatorEntry.COLUMN_ITEM_TRADABLE, item.isTradable() ? 1 : 0);
        cv.put(CalculatorEntry.COLUMN_ITEM_CRAFTABLE, item.isCraftable() ? 1 : 0);
        cv.put(CalculatorEntry.COLUMN_PRICE_INDEX, item.getPriceIndex());
        cv.put(CalculatorEntry.COLUMN_AUSTRALIUM, item.isAustralium() ? 1 : 0);
        cv.put(CalculatorEntry.COLUMN_WEAPON_WEAR, item.getWeaponWear());
        cv.put(CalculatorEntry.COLUMN_COUNT, 1);

        context.getContentResolver().insert(CalculatorEntry.CONTENT_URI, cv);
    }

    public static void createSimpleNotification(Context context, int id, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(id, builder.build());
    }

    public static void notifyPricesWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context.getApplicationContext(), FavoritesWidget.class));
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }

        //overkill?
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public static PopupMenu createItemPopupMenu(final Activity activity, View anchor, final Item item) {
        PopupMenu menu = new PopupMenu(activity, anchor);

        menu.getMenuInflater().inflate(R.menu.popup_item, menu.getMenu());

        menu.getMenu().findItem(R.id.favorite).setTitle(
                isFavorite(activity, item) ? "Remove from favorites" : "Add to favorites");

        menu.getMenu().findItem(R.id.calculator).setEnabled(!isInCalculator(activity, item));

        menu.setOnMenuItemClickListener(menuItem -> {
            Intent intent;
            switch (menuItem.getItemId()) {
                case R.id.history:
                    intent = new Intent(activity, PriceHistoryActivity.class);
                    intent.putExtra(PriceHistoryActivity.EXTRA_ITEM, item);
                    activity.startActivity(intent);
                    break;
                case R.id.favorite:
                    if (isFavorite(activity, item)) {
                        removeFromFavorites(activity, item);
                    } else {
                        addToFavorites(activity, item);
                    }
                    break;
                case R.id.calculator:
                    addToCalculator(activity, item);
                    menuItem.setEnabled(false);
                    break;
                case R.id.backpack_tf:
                    CustomTabActivityHelper.openCustomTab(activity,
                            new CustomTabsIntent.Builder().build(),
                            Uri.parse(item.getBackpackTfUrl()),
                            new WebViewFallback());
                    break;
                case R.id.wiki:
                    CustomTabActivityHelper.openCustomTab(activity,
                            new CustomTabsIntent.Builder().build(),
                            Uri.parse(item.getTf2WikiUrl()),
                            new WebViewFallback());
                    break;
                case R.id.tf2outpost:
                    intent = new Intent(Intent.ACTION_VIEW, buildTf2OutpostSearchUrl(activity, item));
                    activity.startActivity(intent);
                    break;
                case R.id.bazaar_tf:
                    intent = new Intent(Intent.ACTION_VIEW, buildBazaarSearchUrl(activity, item));
                    activity.startActivity(intent);
                    break;
            }
            return true;
        });

        return menu;
    }

    public static Uri buildTf2OutpostSearchUrl(Context context, Item item) {
        Uri.Builder builder = Uri.parse(context.getString(R.string.main_host) +
                context.getString(R.string.link_search_outpost)
        ).buildUpon()
                .appendQueryParameter("defindex", String.valueOf(item.getDefindex()))
                .appendQueryParameter("quality", String.valueOf(item.getQuality()))
                .appendQueryParameter("uncraftable", item.isCraftable() ? "0" : "1")
                .appendQueryParameter("australium", item.isAustralium() ? "1" : "0");

        if (item.canHaveEffects()) {
            builder.appendQueryParameter("effect", String.valueOf(item.getPriceIndex()));
        } else if (item.getPriceIndex() > 0) {
            builder.appendQueryParameter("crate", String.valueOf(item.getPriceIndex()));
        }

        return builder.build();
    }

    public static Uri buildBazaarSearchUrl(Context context, Item item) {
        Uri.Builder builder = Uri.parse(context.getString(R.string.link_search_bazaar)).buildUpon()
                .appendQueryParameter("sg", "440")
                .appendQueryParameter("s", String.valueOf(item.getDefindex()));

        String sp = "quality=" + String.valueOf(item.getQuality());

        if (!item.isCraftable()) {
            sp += ",flag_cannot_craft=1";
        }

        if (item.canHaveEffects()) {
            sp += ",effect=" + String.valueOf(item.getPriceIndex());
        } else if (item.getPriceIndex() > 0) {
            sp += ",crate=" + String.valueOf(item.getPriceIndex());
        }

        return builder.appendQueryParameter("sp", sp).build();
    }
}

