package com.tlongdev.bktf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tlongdev.bktf.enums.Quality;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class (static only).
 */
public class Utility {

    public static final String LOG_TAG = Utility.class.getSimpleName();

    public static final String CURRENCY_USD = "usd";
    public static final String CURRENCY_METAL = "metal";
    public static final String CURRENCY_KEY = "keys";
    public static final String CURRENCY_BUD = "earbuds";

    /**
     * Convenient method for getting the steamId (or vanity user name) of the user.
     */
    public static String getSteamId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String steamId = prefs.getString(context.getString(R.string.pref_steam_id), null);
        if (steamId != null && steamId.equals("")){
            return null;
        }
        return steamId;
    }

    /**
     * Convenient method for getting the steamId of the user.
     */
    public static String getResolvedSteamId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_resolved_steam_id), null);
    }

    /**
     * Properly formats the item name according to its properties.
     */
    public static String formatItemName(String name, int tradable, int craftable, int quality, int index) {
        String formattedName = "";

        if (tradable == 0) {
            formattedName += "Non-Tradable ";
        }
        if (craftable == 0) {
            formattedName += "Non-Craftable ";
        }

        //Convert the quality int to enum for better readability
        Quality q = Quality.values()[quality];

        switch (q) {
            case NORMAL:
                formattedName += "Normal ";
                break;
            case GENUINE:
                formattedName += "Genuine ";
                break;
            case VINTAGE:
                formattedName += "Vintage ";
                break;
            case UNIQUE:
                if (index > 0) //A unique item with a number
                  name = name + " #" + index;
                break;
            case UNUSUAL:
                formattedName += getUnusualEffectName(index) + " ";
                break;
            case COMMUNITY:
                formattedName += "Community ";
                break;
            case VALVE:
                formattedName += "Valve ";
                break;
            case SELF_MADE:
                formattedName += "Self-made ";
                break;
            case STRANGE:
                formattedName += "Strange ";
                break;
            case HAUNTED:
                formattedName += "Haunted ";
                break;
            case COLLECTORS:
                formattedName += "Collector's ";
                break;
        }

        return  formattedName + name;
    }

    public static String formatSimpleItemName(String name, int quality, int index, boolean isProper) {
        String formattedName = "";

        //Convert the quality int to enum for better readability
        Quality q = Quality.values()[quality];

        switch (q) {
            case NORMAL:
                formattedName += "Normal ";
                break;
            case GENUINE:
                formattedName += "Genuine ";
                break;
            case VINTAGE:
                formattedName += "Vintage ";
                break;
            case UNIQUE:
                if (index > 0) //A unique item with a number
                    name = name + " #" + index;

                if (isProper){
                    name = "The " + name;
                }
                break;
            case UNUSUAL:
                formattedName += "Unusual ";
                break;
            case COMMUNITY:
                formattedName += "Community ";
                break;
            case VALVE:
                formattedName += "Valve ";
                break;
            case SELF_MADE:
                formattedName += "Self-made ";
                break;
            case STRANGE:
                formattedName += "Strange ";
                break;
            case HAUNTED:
                formattedName += "Haunted ";
                break;
            case COLLECTORS:
                formattedName += "Collector's ";
                break;
        }

        return  formattedName + name;
    }

    /**
     * Get the proper name of the unusual effect
     */
    public static String getUnusualEffectName(int index) {
        switch (index) {
            case 4:
                return "Community Sparkle";
            case 5:
                return "Holy Glow";
            case 6:
                return "Green Confetti";
            case 7:
                return "Purple Confetti";
            case 8:
                return "Haunted Ghosts";
            case 9:
                return "Green Energy";
            case 10:
                return "Purple Energy";
            case 11:
                return "Circling TF Logo";
            case 12:
                return "Massed Flies";
            case 13:
                return "Burning Flames";
            case 14:
                return "Scorching Flames";
            case 15:
                return "Searing Plasma";
            case 16:
                return "Vivid Plasma";
            case 17:
                return "Sunbeams";
            case 18:
                return "Circling Peace Sign";
            case 19:
                return "Circling Heart";
            case 29:
                return "Stormy Storm";
            case 30:
                return "Blizzardy Storm";
            case 31:
                return "Nuts n' Bolts";
            case 32:
                return "Orbiting Planets";
            case 33:
                return "Orbiting Fire";
            case 34:
                return "Bubbling";
            case 35:
                return "Smoking";
            case 36:
                return "Steaming";
            case 37:
                return "Flaming Lantern";
            case 38:
                return "Cloudy Moon";
            case 39:
                return "Cauldron Bubbles";
            case 40:
                return "Eerie Orbiting Fire";
            case 43:
                return "Knifestorm";
            case 44:
                return "Misty Skull";
            case 45:
                return "Harvest Moon";
            case 46:
                return "It's A Secret To Everybody";
            case 47:
                return "Stormy 13th Hour";
            case 56:
                return "Kill-a-Watt";
            case 57:
                return "Terror-Watt";
            case 58:
                return "Cloud 9";
            case 59:
                return "Aces High";
            case 60:
                return "Dead Presidents";
            case 61:
                return "Miami Nights";
            case 62:
                return "Disco Beat Down";
            case 63:
                return "Phosphorous";
            case 64:
                return "Sulphurous";
            case 65:
                return "Memory Leak";
            case 66:
                return "Overclocked";
            case 67:
                return "Electrostatic";
            case 68:
                return "Power Surge";
            case 69:
                return "Anti-Freeze";
            case 70:
                return "Time Warp";
            case 71:
                return "Green Black Hole";
            case 72:
                return "Roboactive";
            case 73:
                return "Arcana";
            case 74:
                return "Spellbound";
            case 75:
                return "Chiroptera Venenata";
            case 76:
                return "Poisoned Shadows";
            case 77:
                return "Something Burning This Way Comes";
            case 78:
                return "Hellfire";
            case 79:
                return "Darkblaze";
            case 80:
                return "Demonflame";
            case 81:
                return "Bonzo The All-Gnawing";
            case 82:
                return "Amaranthine";
            case 83:
                return "Stare From Beyond";
            case 84:
                return "The Ooze";
            case 85:
                return "Ghastly Ghosts Jr";
            case 86:
                return "Haunted Phantasm Jr";
            case 87:
                return "Frostbite";
            case 88:
                return "Molten Mallard";
            case 89:
                return "Morning Glory";
            case 90:
                return "Death at Dusk";
            case 3001:
                return "Showstopper";
            case 3003:
                return "Holy Grail";
            case 3004:
                return "'72";
            case 3005:
                return "Fountain of Delight";
            case 3006:
                return "Screaming Tiger";
            case 3007:
                return "Skill Gotten Gains";
            case 3008:
                return "Midnight Whirlwind";
            case 3009:
                return "Silver Cyclone";
            case 3010:
                return "Mega Strike";
            case 3011:
                return "Haunted Phantasm";
            case 3012:
                return "Ghastly Ghosts";
            default:
                return "";
        }
    }

    /**
     * Returns a drawable based on the item's properties to use asa background
     */
    public static LayerDrawable getItemBackground(Context context, int quality, int tradable, int craftable) {
        //Convert the quality int to enum for better readability
        Quality q = Quality.values()[quality];

        Drawable itemFrame;
        Drawable craftableFrame;
        Drawable tradableFrame;

        switch (q) {
            case GENUINE:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_genuine);
                break;
            case VINTAGE:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_vintage);
                break;
            case UNUSUAL:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_unusual);
                break;
            case UNIQUE:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_unique);
                break;
            case COMMUNITY:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_community);
                break;
            case VALVE:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_valve);
                break;
            case SELF_MADE:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_community);
                break;
            case STRANGE:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_strange);
                break;
            case HAUNTED:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_haunted);
                break;
            case COLLECTORS:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_collectors);
                break;
            default:
                itemFrame =  context.getResources().getDrawable(R.drawable.item_background_normal);
                break;
        }
        if (craftable == 0){
            craftableFrame = context.getResources().getDrawable(R.drawable.non_craftable_border);
        }
        else {
            //Easier to use an empty drawable instead of resizing the layer drawable
            craftableFrame = context.getResources().getDrawable(R.drawable.empty_drawable);
        }

        if (tradable == 0){
            tradableFrame = context.getResources().getDrawable(R.drawable.non_tradable_border);
        }
        else {
            //Easier to use an empty drawable instead of resizing the layer drawable
            tradableFrame = context.getResources().getDrawable(R.drawable.empty_drawable);
        }

        return new LayerDrawable(new Drawable[] {itemFrame, craftableFrame, tradableFrame});
    }

    /**
     * Formats the given price and converts it to the desired currency.
     */
    public static String formatPrice(Context context, double low, double high, String originalCurrency, String targetCurrency, boolean twoLines) throws Throwable {
        //Initial string
        String product = "";

        //Convert the prices first
        low = convertPrice(context, low, originalCurrency, targetCurrency);
        if (high > 0.0)
            high = convertPrice(context, high, originalCurrency, targetCurrency);

        //Check if the price is an int
        if ((int)low == low)
            product += (int)low;
        //Check if the double has fraction smaller than 0.01, if so we need to format the double
        else if (("" + low).substring(("" + low).indexOf('.') + 1).length() > 2)
            product += new DecimalFormat("#0.00").format(low);
        else
            product += low;

        if (high > 0.0){
            //Check if the price is an int
            if ((int)high == high)
                product += "-" + (int)high;
            //Check if the double has fraction smaller than 0.01, if so we need to format the double
            else if (("" + high).substring(("" + high).indexOf('.') + 1).length() > 2)
                product += "-" + new DecimalFormat("#0.00").format(high);
            else
                product += "-" + high;
        }

        //If the price needs to be in two lines, the currency will be in a seperate line.
        if (twoLines) {
            product += "\n";
        }

        //Append the string with the proper currency
        switch(targetCurrency) {
            case CURRENCY_BUD:
                if (low == 1.0 && high == 0.0)
                    return product + " bud";
                else
                    return product + " buds";
            case CURRENCY_METAL:
                return product + " ref";
            case CURRENCY_KEY:
                if (low == 1.0 && high == 0.0)
                    return product + " key";
                else
                    return product + " keys";
            case CURRENCY_USD:
                return "$" + product;
            default:
                //App should never reach this code
                if (isDebugging(context))
                    Log.e(LOG_TAG, "Error formatting price");
                throw new Throwable("Error while formatting price");
        }
    }

    /**
     * Converts the price to that desired currency.
     */
    public static double convertPrice(Context context, double price, String originalCurrency, String targetCurrency) throws Throwable {

        if (originalCurrency.equals(targetCurrency))
            //The target currency equals the original currency, nothing to do.
            return price;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //Magic converter block
        switch(originalCurrency){
            case CURRENCY_BUD:
                switch(targetCurrency){
                    case CURRENCY_KEY:
                        return price * (getDouble(prefs, context.getString(R.string.pref_buds_raw), 1) 
                                / getDouble(prefs, context.getString(R.string.pref_key_raw), 1));
                    case CURRENCY_METAL:
                        return price * getDouble(prefs, context.getString(R.string.pref_buds_raw), 1);
                    case CURRENCY_USD:
                        return price * (getDouble(prefs, context.getString(R.string.pref_buds_raw), 1)
                                * getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1));
                }
            case CURRENCY_METAL:
                switch(targetCurrency){
                    case CURRENCY_KEY:
                        return price / getDouble(prefs, context.getString(R.string.pref_key_raw), 1);
                    case CURRENCY_BUD:
                        return price / getDouble(prefs, context.getString(R.string.pref_buds_raw), 1);
                    case CURRENCY_USD:
                        return price * getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1);
                }
            case CURRENCY_KEY:
                switch(targetCurrency){
                    case CURRENCY_METAL:
                        return price * getDouble(prefs, context.getString(R.string.pref_key_raw), 1);
                    case CURRENCY_BUD:
                        return price * (getDouble(prefs, context.getString(R.string.pref_key_raw), 1)
                                / getDouble(prefs, context.getString(R.string.pref_buds_raw), 1));
                    case CURRENCY_USD:
                        return price * getDouble(prefs, context.getString(R.string.pref_key_raw), 1)
                                * getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1);
                }
            case CURRENCY_USD:
                switch(targetCurrency){
                    case CURRENCY_METAL:
                        return price / getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1);
                    case CURRENCY_BUD:
                        return price / getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1)
                                / getDouble(prefs, context.getString(R.string.pref_buds_raw), 1);
                    case CURRENCY_KEY:
                        return price * getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1)
                                / getDouble(prefs, context.getString(R.string.pref_key_raw), 1);
                }
            default:
                String error = "Unknown currency: " + originalCurrency + " - " + targetCurrency;
                if (isDebugging(context))
                    Log.e(LOG_TAG, error);
                throw new Throwable(error);
        }
    }

    /**
     * Check if the given steamId is a 64bit steamId using Regex.
     */
    public static boolean isSteamId(String id) {
        return id.matches("7656119[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]");
    }

    /**
     * Format the unix timestamp the a user readable string.
     */
    public static String formatUnixTimeStamp(long unixSeconds){
        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * Format the timestamp to a user friendly string that is the same as on steam profile pages.
     */
    public static String formatLastOnlineTime(long time) {
        //If the time is longer than 2 days tho format is X days.
        if (time >= 172800000L) {
            long days = time / 86400000;
            return "" + days + " days ago";
        }
        //If the time is longer than an hour, the format is X hour(s) Y minute(s)
        if (time >= 3600000L) {
            long hours = time / 3600000;
            if (time % 3600000L == 0) {
                if (hours == 1)
                    return "" + hours + " hour ago";
                else {
                    return "" + hours + " hours ago";
                }
            }
            else {
                long minutes = (time % 3600000L) / 60000;
                if (hours == 1)
                    if (minutes == 1)
                        return "" + hours + " hour " + minutes + " minute ago";
                    else
                        return "" + hours + " hour " + minutes + " minutes ago";
                else {
                    if (minutes == 1)
                        return "" + hours + " hours " + minutes + " minute ago";
                    else
                        return "" + hours + " hours " + minutes + " minutes ago";
                }
            }
        }
        //Else it was less than an hour ago, the format is X minute(s).
        else {
            long minutes = time / 60000;
            if (minutes == 0){
                return "Just now";
            } else if (minutes == 1){
                return "1 minute ago";
            } else {
                return "" + minutes + " minutes ago";
            }
        }
    }

    /**
     * Retrieves the steamId from a properly formatted JSON.
     */
    public static String parseSteamIdFromVanityJson(String userJsonStr) throws JSONException {
        final String OWM_RESPONSE = "response";
        final String OWM_SUCCESS = "success";
        final String OWM_STEAM_ID = "steamid";
        final String OWM_MESSAGE = "message";

        JSONObject jsonObject = new JSONObject(userJsonStr);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);

        if (response.getInt(OWM_SUCCESS) != 1){
            //Return the error message if unsuccessful.
            return response.getString(OWM_MESSAGE);
        }

        return response.getString(OWM_STEAM_ID);
    }

    /**
     * Retrieves the user name from a properly formatted JSON.
     */
    public static String parseUserNameFromJson(String jsonString) throws JSONException {
        final String OWM_RESPONSE = "response";
        final String OWM_PLAYERS = "players";
        final String OWM_NAME = "personaname";

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);
        JSONArray players = response.getJSONArray(OWM_PLAYERS);
        JSONObject player = players.getJSONObject(0);

        return player.getString(OWM_NAME);
    }

    /**
     * Retrieves the url for the user avatar from a proerly formatted JSON.
     */
    public static String parseAvatarUrlFromJson(String jsonString) throws JSONException {
        final String OWM_RESPONSE = "response";
        final String OWM_PLAYERS = "players";
        final String OWM_AVATAR = "avatarfull";

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject response = jsonObject.getJSONObject(OWM_RESPONSE);
        JSONArray players = response.getJSONArray(OWM_PLAYERS);
        JSONObject player = players.getJSONObject(0);

        return player.getString(OWM_AVATAR);
    }

    /**
     * Check whether the user if connected to the internet.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Convenient method for storing double values in shared preferences.
     */
    public static SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    /**
     * Convenient method for getting double values from shared preferences.
     */
    public static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    /**
     * Whether we should log or not
     */
    public static boolean isDebugging(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_debug), false);
    }

    /**
     * Check whether the given timestamp is older than 3 months
     */
    public static boolean isPriceOld(int unixTimeStamp){
        return System.currentTimeMillis() - unixTimeStamp*1000L > 7884000000L;
    }

    /**
     * Rounds the given double
     */
    public static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double getRawMetal(int rawRef, int rawRec, int rawScraps) {
        return (1.0/9.0 * rawScraps) + (1.0/3.0 * rawRec) + rawRef;
    }

    public static String getPaintName(int index){
        switch (index){
            case 7511618: return "Indubitably Green";
            case 4345659: return "Zepheniah's Greed";
            case 5322826: return "Noble Hatter's Violet";
            case 14204632: return "Color No. 216-190-216";
            case 8208497: return "Deep Commitment to Purple";
            case 13595446: return "Mann Co. Orange";
            case 10843461: return "Muskelmannbraun";
            case 12955537: return "Peculiarly Drab Tincture";
            case 6901050: return "Radigan Conagher Brown";
            case 8154199: return "Ye Olde Rustic Color";
            case 15185211: return "Australium Gold";
            case 8289918: return "Aged Moustache Grey";
            case 15132390: return "An Extraordinary Abundance of Tinge";
            case 1315860: return "A Distinctive Lack of Hue";
            case 16738740: return "Pink as Hell";
            case 3100495: return "Color Similar to Slate";
            case 8421376: return "Drably Olive";
            case 3329330: return "The Bitter Taste of Defeat and Lime";
            case 15787660: return "The Color of a Gentlemann's Business Pants";
            case 15308410: return "Salmon Injustice";
            case 12073019: return "Team Spirit";
            case 4732984: return "Operator's Overalls";
            case 11049612: return "Waterlogged Lab Coat";
            case 3874595: return "Balaclava's Are Forever";
            case 6637376: return "Air of Debonair";
            case 8400928: return "The Value of Teamwork";
            case 12807213: return "Cream Spirit";
            case 2960676: return "After Eight";
            case 12377523: return "A Mann's Mint";
            default: return null;
        }
    }

    public static boolean isPaint(int index){
        switch (index){
            case 7511618: return true;
            case 4345659: return true;
            case 5322826: return true;
            case 14204632: return true;
            case 8208497: return true;
            case 13595446: return true;
            case 10843461: return true;
            case 12955537: return true;
            case 6901050: return true;
            case 8154199: return true;
            case 15185211: return true;
            case 8289918: return true;
            case 15132390: return true;
            case 1315860: return true;
            case 16738740: return true;
            case 3100495: return true;
            case 8421376: return true;
            case 3329330: return true;
            case 15787660: return true;
            case 15308410: return true;
            case 12073019: return true;
            case 4732984: return true;
            case 11049612: return true;
            case 3874595: return true;
            case 6637376: return true;
            case 8400928: return true;
            case 12807213: return true;
            case 2960676: return true;
            case 12377523: return true;
            default: return false;
        }
    }

    public static boolean canHaveEffects(int defindex, int quality){
        if (quality == 5 || quality == 7 || quality == 9) {
            return defindex != 267 && defindex != 266;
        } else if (defindex == 1899 || defindex == 125){
            return true;
        }
        return false;
    }

    public static int fixDefindex(int defindex) {
        //Check if the defindex is of a duplicate defindex to provide the proper price for it.
        switch (defindex){
            case 9: case 10: case 11: case 12: //duplicate shotguns
                return 9;
            case 23: //duplicate pistol
                return  22;
            case 28: //duplicate destruction tool
                return 26;
            case 190: case 191: case 192: case 193: case 194: //duplicate stock weapons
            case 195: case 196: case 197: case 198: case 199:
                return defindex - 190;
            case 200: case 201: case 202: case 203: case 204: //duplicate stock weapons
            case 205: case 206: case 207: case 208: case 209:
                return defindex - 187;
            case 210:
                return defindex - 186;
            case 211: case 212:
                return defindex - 182;
            case 736: //duplicate sapper
                return 735;
            case 737: //duplicate construction pda
                return 25;
            case 5041: case 5045: //duplicate crates
                return 5022;
            case 5735: case 5742: case 5752: case 5781: case 5802: //duplicate munitions
                return 5734;
            default:
                return defindex;
        }
    }

    public static int getIconIndex(int defindex) {
        //Check if the defindex is of a duplicate defindex to provide the proper price for it.
        switch (defindex){
            case 9: case 10: case 11: case 12: //duplicate shotguns
                return 9;
            case 23: //duplicate pistol
                return  22;
            case 28: //duplicate destruction tool
                return 26;
            case 190: case 191: case 192: case 193: case 194: //duplicate stock weapons
            case 195: case 196: case 197: case 198: case 199:
                return defindex - 190;
            case 200: case 201: case 202: case 203: case 204: //duplicate stock weapons
            case 205: case 206: case 207: case 208: case 209:
                return defindex - 187;
            case 210:
                return defindex - 186;
            case 211: case 212:
                return defindex - 182;
            case 294: //lugermorph
                return 160;
            case 422: //companion cube pin
                return 299;
            case 681: case 682: case 683: case 684: //gold cup medal
                return 680;
            case 686: case 687: case 688: case 689: //silver cup medal
                return 685;
            case 691: case 692: case 693: case 694: case 695: case 696: case 697: case 698: //bronze cup medal
                return 690;
            case 736: //duplicate sapper
                return 735;
            case 737: //duplicate construction pda
                return 25;
            case 744: //pyrovision goggles
                return 743;
            case 791: case 928://pink promo gifts
                return 790;
            case 831: case 832: case 833: case 834: // promoasian items
            case 835: case 836: case 837: case 838:
                return defindex - 21;
            case 839: //gift bag
                return 729;
            case 1132: //spell magazine
                return 1070;
            case 2015: case 2049: case 2079: case 2123: case 2125: //map stamps packs
                return 2007;
            case 2035: case 2036: case 2037: case 2038: case 2039: //dr gorbort pack
                return 2034;
            case 2041: case 2045: case 2047: case 2048: //deus ex pack
                return 2040;
            case 2042: case 2095: //soldier pack
                return 2019;
            case 2043: case 2044: //more soldier packs
                return 2042;
            case 2046: //shogun pack
                return 2016;
            case 2070: //promo chicken hat
                return 785;
            case 2081: //hong kong pac
                return 2076;
            case 2094: case 2096: case 2097: case 2098: case 2099:
            case 2100: case 2101: case 2102: //class packs
                return defindex - 76;
            case 2103: //deus Ex arm
                return 524;
            case 5020: //desc tag
                return 2093;
            case 5041: case 5045: //duplicate crates
                return 5022;
            case 5049: case 5067: case 5072: case 5073: case 5079: case 5081: case 5628: case 5631:
            case 5632: case 5713: case 5716: case 5717: case 5762: case 5791: case 5792:
                return 5021; //mann co supply crate key
            case 5074: //something special for someone special
                return 699;
            case 5601: case 5602: case 5603: case 5604: //pyro dusts
                return 5600;
            case 5721: case 5722: case 5723: case 5724: case 5725: case 5753: case 5754: case 5755:
            case 5756: case 5757: case 5758: case 5759: case 5783: case 5784: case 6522:
                return 5661; //strangifiers
            case 5727: case 5728: case 5729: case 5730: case 5731: case 5732: case 5733:
            case 5743: case 5744: case 5745: case 5746: case 5747: case 5748: case 5749: case 5750:
            case 5751: case 5793: case 5794: case 5795: case 5796: case 5797: case 5798: case 5799:
            case 5800: case 5801: case 6527: //killstreak kits
                return 5726;
            case 5735: case 5742: case 5752: case 5781: case 5802: //duplicate munitions
                return 5734;
            case 5738: //sombrero crate
                return 5737;
            case 5773: //halloween cauldron
                return 5772;
            case 8003: case 8006: //ugc medal
                return 8000;
            case 8004: case 8007: //ugc medal
                return 8001;
            case 8005: case 8008: //ugc medal
                return 8002;
            case 8012: //ugc medal
                return 8009;
            case 8013: //ugc medal
                return 8010;
            case 8014: //ugc medal
                return 8011;
            case 8018: case 8021: //ugc medal
                return 8015;
            case 8019: case 8022: //ugc medal
                return 8016;
            case 8020: case 8023: //ugc medal
                return 8017;
            case 8031: case 8035: case 8039: case 8043: case 8047: case 8051: case 8055:
            case 8058: case 8062: case 8066: //esl gold medal
                return 8027;
            case 8032: case 8036: case 8040: case 8044: case 8048: case 8052: case 8056:
            case 8059: case 8063: case 8067: //esl silver medal
                return 8028;
            case 8033: case 8037: case 8041: case 8045: case 8049: case 8053: case 8057:
            case 8064: case 8068: //esl bronze medal
                return 8029;
            case 8034: case 8038: case 8042: case 8046: case 8050: case 8054: case 8060:
            case 8061: case 8065: case 8069: //esl platinum medal
                return 8030;
            case 8071: //ready steady pan s1 medal
                return 8070;
            case 8075: case 8078: case 8081: case 8084: case 8087: case 8275: case 8307:
            case 8323: case 8397: //star medal gold
                return 8072;
            case 8076: case 8079: case 8082: case 8085: case 8088: case 8276: case 8308:
            case 8324: case 8398: //star medal silver
                return 8073;
            case 8077: case 8080: case 8083: case 8086: case 8089: case 8277: case 8309:
            case 8325: case 8399: //start medal bronze
                return 8074;
            case 8091: case 8092: case 8093: case 8094: case 8095: case 8096: case 8097:
            case 8098: case 8099: case 8100: case 8101: case 8102: case 8103: case 8104:
            case 8105: case 8106: case 8107: case 8108: case 8109: case 8110: case 8111:
            case 8112: case 8113: case 8114: case 8115: case 8116: case 8117: case 8118:
            case 8119: case 8120: case 8121: case 8122: case 8123: case 8124: case 8125:
            case 8278: case 8279: case 8280: case 8281: case 8282: case 8283: case 8310:
            case 8311: case 8312: case 8313: case 8314: case 8315: case 8326: case 8327:
            case 8328: case 8329: case 8330: case 8331: case 8400: case 8401: case 8402:
            case 8403: case 8404: //star medal participant
                return 8090;
            case 8127: case 8128: case 8129: case 8130: case 8131: case 8132: case 8133:
            case 8134: case 8135: case 8136: case 8137: case 8138: case 8139: case 8140:
            case 8141: case 8142: case 8143: case 8144: case 8145: case 8146: case 8147:
            case 8148: case 8149: case 8150: case 8151: case 8152: case 8153: case 8154:
            case 8155: case 8156: case 8157: case 8158: case 8159: case 8160: case 8161:
            case 8162: case 8163: case 8164: case 8165: case 8166: case 8167: case 8284:
            case 8285: case 8286: case 8287: case 8288: case 8289: case 8290: case 8316:
            case 8317: case 8318: case 8319: case 8320: case 8321: case 8322: case 8332:
            case 8333: case 8334: case 8335: case 8336: case 8337: case 8338: case 8406:
            case 8407: case 8408: case 8409: case 8410: case 8411: case 8412: //star bottle cap
                return 8126;
            case 8223: //duplicate soldier medal
                return 121;
            default: //don't change
                return defindex;
        }
    }

    public static class IntegerPair{
        int x;
        int y;

        public IntegerPair(){
            this(0);
        }

        public IntegerPair(int x) {
            this(x, 0);
        }

        public IntegerPair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }
}

