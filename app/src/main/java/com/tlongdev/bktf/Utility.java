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
        /*if (defindex >= 9 && defindex <= 12) { //duplicate shotguns
            defindex = 9;
        } else if (defindex == 23) {//duplicate pistol
            defindex = 22;
        } else if (defindex == 28) {//duplicate destruction tool
            defindex = 26;
        } else if (defindex >= 190 && defindex <= 199) { //duplicate stock weapons
            defindex -= 190;
        } else if (defindex >= 200 && defindex <= 209) {
            defindex -= 187;
        } else if (defindex == 210) {
            defindex -= 186;
        } else if (defindex == 211 || defindex == 212) {
            defindex -= 182;
        } else if (defindex == 736) { //duplicate sapper
            defindex = 735;
        } else if (defindex == 737) { //duplicate construction pda
            defindex = 25;
        } else if (defindex == 5041 || defindex == 5045) { //duplicate crates
            defindex = 5022;
        } else if (defindex == 5735 || defindex == 5742 //duplicate munitions
                || defindex == 5752 || defindex == 5781 || defindex == 5802) {
            defindex = 5734;
        }*/
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
            case 736: //duplicate sapper
                return 735;
            case 737: //duplicate construction pda
                return 25;
            case 5041: case 5045: //duplicate crates
                return 5022;
            case 5735: case 5742: case 5752: case 5781: case 5802: //duplicate munitions
                return 5734;
            case 8223: //duplicate soldier medal
                return 121;
            default:
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

