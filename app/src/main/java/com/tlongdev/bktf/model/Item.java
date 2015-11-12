package com.tlongdev.bktf.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;

import java.io.IOException;
import java.io.InputStream;

/**
 * Item class
 */
public class Item {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = Item.class.getSimpleName();

    private int defindex;

    private String name;

    private int quality;

    private boolean tradable;

    private boolean craftable;

    private boolean australium;

    private int priceIndex;

    private int weaponWear;

    private Price price;

    public Item() {
        this(0, null, 0, false, false, false, 0, null);
    }

    public Item(int defindex, String name, int quality, boolean tradable, boolean craftable, boolean australium, int priceIndex, Price price) {
        this(defindex, name, quality, tradable, craftable, australium, priceIndex, -1, price);
    }

    public Item(int defindex, String name, int quality, boolean tradable, boolean craftable, boolean australium, int priceIndex, int weaponWear, Price price) {
        this.defindex = defindex;
        this.name = name;
        this.quality = quality;
        this.tradable = tradable;
        this.craftable = craftable;
        this.australium = australium;
        this.priceIndex = priceIndex;
        this.weaponWear = weaponWear;
        this.price = price;
    }

    public int getDefindex() {
        return defindex;
    }

    public void setDefindex(int defindex) {
        this.defindex = defindex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean isTradable() {
        return tradable;
    }

    public void setTradable(boolean tradable) {
        this.tradable = tradable;
    }

    public boolean isCraftable() {
        return craftable;
    }

    public void setCraftable(boolean craftable) {
        this.craftable = craftable;
    }

    public boolean isAustralium() {
        return australium;
    }

    public void setAustralium(boolean australium) {
        this.australium = australium;
    }

    public int getPriceIndex() {
        return priceIndex;
    }

    public void setPriceIndex(int priceIndex) {
        this.priceIndex = priceIndex;
    }

    public int getWeaponWear() {
        return weaponWear;
    }

    public void setWeaponWear(int weaponWear) {
        this.weaponWear = weaponWear;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    /**
     * Properly formats the item name according to its properties.
     *
     * @param context  the context
     * @param isProper whether the name needs the definite article (The)
     * @return the formatted name
     */
    public String getFormattedName(Context context, boolean isProper) {
        //Empty string that will be appended.
        String formattedName = "";

        //Check tradability
        if (!tradable) {
            formattedName += context.getString(R.string.quality_non_tradable) + " ";
        }
        //Check craftability
        if (!craftable) {
            formattedName += context.getString(R.string.quality_non_craftable) + " ";
        }

        //Handle strangifier names differently
        if (defindex == 6522) {
            Cursor itemCursor = context.getContentResolver().query(
                    DatabaseContract.ItemSchemaEntry.CONTENT_URI,
                    new String[]{ItemSchemaEntry.COLUMN_ITEM_NAME, ItemSchemaEntry.COLUMN_TYPE_NAME, ItemSchemaEntry.COLUMN_PROPER_NAME},
                    ItemSchemaEntry.TABLE_NAME + "." + ItemSchemaEntry.COLUMN_DEFINDEX + " = ?",
                    new String[]{String.valueOf(defindex)},
                    null
            );

            if (itemCursor != null) {
                if (itemCursor.moveToFirst()) {
                    formattedName += itemCursor.getString(0) + " " + name;
                }
                itemCursor.close();
            }

            return formattedName;
        } else
            //TODO Handle chemistry set names differently
            if (defindex == 20001) {

            }

        //Switch case for the quality
        switch (quality) {
            case Quality.NORMAL:
                formattedName += context.getString(R.string.quality_normal) + " ";
                break;
            case Quality.GENUINE:
                formattedName += context.getString(R.string.quality_genuine) + " ";
                break;
            case Quality.VINTAGE:
                formattedName += context.getString(R.string.quality_vintage) + " ";
                break;
            case Quality.UNIQUE:
                if (priceIndex > 0) //A unique item with a number
                    name = name + " #" + priceIndex;
                break;
            case Quality.UNUSUAL:
                //Get the unusual effect name by its index
                formattedName += Utility.getUnusualEffectName(context, priceIndex) + " ";
                break;
            case Quality.COMMUNITY:
                formattedName += context.getString(R.string.quality_community) + " ";
                break;
            case Quality.VALVE:
                formattedName += context.getString(R.string.quality_valve) + " ";
                break;
            case Quality.SELF_MADE:
                formattedName += context.getString(R.string.quality_self_made) + " ";
                break;
            case Quality.STRANGE:
                formattedName += context.getString(R.string.quality_strange) + " ";
                break;
            case Quality.HAUNTED:
                formattedName += context.getString(R.string.quality_haunted) + " ";
                break;
            case Quality.COLLECTORS:
                formattedName += context.getString(R.string.quality_collectors) + " ";
                break;
            default:
                formattedName += context.getString(R.string.quality_normal) + " ";
                break;
        }

        //Append the item name to the end.
        return isProper ? formattedName + "The " + name : formattedName + name;
    }

    /**
     * Properly formats the item name according to its properties.
     *
     * @param context the context
     * @return the formatted name
     */
    public String getFormattedName(Context context) {
        return getFormattedName(context, false);
    }

    /**
     * Properly formats the item name according to its properties. Simple version.
     *
     * @param context  the context
     * @param isProper whether the name needs the definite article (The)
     * @return the formatted name
     */
    public String getSimpleFormattedName(Context context, boolean isProper) {
        if (quality == Quality.UNUSUAL) {
            return context.getString(R.string.quality_unusual) + " " + name;
        } else if (isProper && quality == Quality.UNIQUE) {
            return getFormattedName(context, true);
        } else {
            return getFormattedName(context, false);
        }
    }

    /**
     * Properly formats the item name according to its properties. Simple version.
     *
     * @param context the context
     * @return the formatted name
     */
    public String getSimpleFormattedName(Context context) {
        return getSimpleFormattedName(context, false);
    }

    /**
     * Check whether the item can have particle effects.
     *
     * @return true if the item can have particle effects
     */
    public boolean canHaveEffects() {
        if (defindex >= 15000 && defindex <= 15059) {
            return false; //TODO weapon effects disabled for now
        }
        //Unusuals, self-made and community items
        if (quality == 5 || quality == 7 || quality == 9) {
            return defindex != 267 && defindex != 266;
        } else if (defindex == 1899 || defindex == 125) { //Cheater's Lament and Traveler's Hat
            return true;
        }
        return false;
    }

    /**
     * Returns quality color of the item.
     *
     * @param context the context
     * @param isDark  whether to return the dark version of the color
     * @return the color of the item
     */
    public int getColor(Context context, boolean isDark) {
        switch (quality) {
            case Quality.GENUINE:
                return isDark ? Utility.getColor(context, R.color.tf2_genuine_color_dark)
                        : Utility.getColor(context, R.color.tf2_genuine_color);
            case Quality.VINTAGE:
                return isDark ? Utility.getColor(context, R.color.tf2_vintage_color_dark)
                        : Utility.getColor(context, R.color.tf2_vintage_color);
            case Quality.UNUSUAL:
                return isDark ? Utility.getColor(context, R.color.tf2_unusual_color_dark)
                        : Utility.getColor(context, R.color.tf2_unusual_color);
            case Quality.UNIQUE:
                return isDark ? Utility.getColor(context, R.color.tf2_unique_color_dark)
                        : Utility.getColor(context, R.color.tf2_unique_color);
            case Quality.COMMUNITY:
                return isDark ? Utility.getColor(context, R.color.tf2_community_color_dark)
                        : Utility.getColor(context, R.color.tf2_community_color);
            case Quality.VALVE:
                return isDark ? Utility.getColor(context, R.color.tf2_valve_color_dark)
                        : Utility.getColor(context, R.color.tf2_valve_color);
            case Quality.SELF_MADE:
                return isDark ? Utility.getColor(context, R.color.tf2_community_color_dark)
                        : Utility.getColor(context, R.color.tf2_community_color);
            case Quality.STRANGE:
                return isDark ? Utility.getColor(context, R.color.tf2_strange_color_dark)
                        : Utility.getColor(context, R.color.tf2_strange_color);
            case Quality.HAUNTED:
                return isDark ? Utility.getColor(context, R.color.tf2_haunted_color_dark)
                        : Utility.getColor(context, R.color.tf2_haunted_color);
            case Quality.COLLECTORS:
                return isDark ? Utility.getColor(context, R.color.tf2_collectors_color_dark)
                        : Utility.getColor(context, R.color.tf2_collectors_color);
            case Quality.PAINTKITWEAPON:
                return getDecoratedWeaponColor(context, isDark);
            default:
                return isDark ? Utility.getColor(context, R.color.tf2_normal_color_dark)
                        : Utility.getColor(context, R.color.tf2_normal_color);
        }
    }

    /**
     * Gets the quality color of a decorated weapon color.
     *
     * @param context the context
     * @param isDark  whether to return the dark version
     * @return the desired color
     */
    private int getDecoratedWeaponColor(Context context, boolean isDark) {
        int colorResource = 0;
        switch (defindex) {
            case 15025:
            case 15026:
            case 15027:
            case 15028:
            case 15029:
            case 15039:
            case 15040:
            case 15041:
            case 15042:
            case 15043:
            case 15044:
                colorResource = isDark ? R.color.tf2_decorated_weapon_civilian_dark : R.color.tf2_decorated_weapon_civilian;
                break;
            case 15020:
            case 15021:
            case 15022:
            case 15023:
            case 15024:
            case 15035:
            case 15036:
            case 15037:
            case 15038:
                colorResource = isDark ? R.color.tf2_decorated_weapon_freelance_dark : R.color.tf2_decorated_weapon_freelance;
                break;
            case 15000:
            case 15001:
            case 15003:
            case 15004:
            case 15005:
            case 15008:
            case 15016:
            case 15017:
            case 15018:
            case 15032:
            case 15033:
            case 15034:
            case 15047:
            case 15054:
            case 15055:
            case 15057:
            case 15058:
                colorResource = isDark ? R.color.tf2_decorated_weapon_mercenary_dark : R.color.tf2_decorated_weapon_mercenary;
                break;
            case 15002:
            case 15006:
            case 15010:
            case 15012:
            case 15015:
            case 15019:
            case 15030:
            case 15031:
            case 15046:
            case 15049:
            case 15050:
            case 15051:
            case 15056:
                colorResource = isDark ? R.color.tf2_decorated_weapon_commando_dark : R.color.tf2_decorated_weapon_commando;
                break;
            case 15007:
            case 15009:
            case 15011:
            case 15048:
            case 15052:
            case 15053:
                colorResource = isDark ? R.color.tf2_decorated_weapon_assassin_dark : R.color.tf2_decorated_weapon_assassin;
                break;
            case 15013:
            case 15014:
            case 15045:
            case 15059:
                colorResource = isDark ? R.color.tf2_decorated_weapon_elite_dark : R.color.tf2_decorated_weapon_elite;
                break;
        }
        return Utility.getColor(context, colorResource);
    }

    /**
     * Gets the decorated weapon description.
     *
     * @param type the type of the decorated weapon
     * @return the formatted description string
     */
    public String getDecoratedWeaponDesc(String type) {
        String wearStr;
        switch (weaponWear) {
            case 1045220557:
                wearStr = "(Factory New)";
                break;
            case 1053609165:
                wearStr = "(Minimal Wear)";
                break;
            case 1058642330:
                wearStr = "(Field-Tested)";
                break;
            case 1061997773:
                wearStr = "(Well Worn)";
                break;
            case 1065353216:
                wearStr = "(Battle Scarred)";
                break;
            default:
                throw new IllegalArgumentException("Invalid wear: " + weaponWear);
        }

        switch (defindex) {
            case 15025:
            case 15026:
            case 15027:
            case 15028:
            case 15029:
            case 15039:
            case 15040:
            case 15041:
            case 15042:
            case 15043:
            case 15044:
                return "Civilian Grade " + type + " " + wearStr;
            case 15020:
            case 15021:
            case 15022:
            case 15023:
            case 15024:
            case 15035:
            case 15036:
            case 15037:
            case 15038:
                return "Freelance Grade " + type + " " + wearStr;
            case 15000:
            case 15001:
            case 15003:
            case 15004:
            case 15005:
            case 15008:
            case 15016:
            case 15017:
            case 15018:
            case 15032:
            case 15033:
            case 15034:
            case 15047:
            case 15054:
            case 15055:
            case 15057:
            case 15058:
                return "Mercenary Grade " + type + " " + wearStr;
            case 15002:
            case 15006:
            case 15010:
            case 15012:
            case 15015:
            case 15019:
            case 15030:
            case 15031:
            case 15046:
            case 15049:
            case 15050:
            case 15051:
            case 15056:
                return "Commando Grade " + type + " " + wearStr;
            case 15007:
            case 15009:
            case 15011:
            case 15048:
            case 15052:
            case 15053:
                return "Assassin Grade " + type + " " + wearStr;
            case 15013:
            case 15014:
            case 15045:
            case 15059:
                return "Elite Grade " + type + " " + wearStr;
            default:
                throw new IllegalArgumentException("Invalid defindex: " + defindex);
        }
    }

    /**
     * This method is for achieving consistency between GetPrices defindexes and GetPlayerItems
     * defindexes. There are some duplicate items, resulting in no prices in backpack.
     *
     * @return common defindex
     */
    public int getFixedDefindex() {
        // TODO: 2015. 10. 26. create a way to auto generate this method
        //Check if the defindex is of a duplicate defindex to provide the proper price for it.
        switch (defindex) {
            case 9:
            case 10:
            case 11:
            case 12: //duplicate shotguns
                return 9;
            case 23: //duplicate pistol
                return 22;
            case 28: //duplicate destruction tool
                return 26;
            case 190:
            case 191:
            case 192:
            case 193:
            case 194: //duplicate stock weapons
            case 195:
            case 196:
            case 197:
            case 198:
            case 199:
                return defindex - 190;
            case 200:
            case 201:
            case 202:
            case 203:
            case 204: //duplicate stock weapons
            case 205:
            case 206:
            case 207:
            case 208:
            case 209:
                return defindex - 187;
            case 210:
                return defindex - 186;
            case 211:
            case 212:
                return defindex - 182;
            case 736: //duplicate sapper
                return 735;
            case 737: //duplicate construction pda
                return 25;
            case 5041:
            case 5045: //duplicate crates
                return 5022;
            case 5735:
            case 5742:
            case 5752:
            case 5781:
            case 5802: //duplicate munitions
                return 5734;
            default:
                return defindex;
        }
    }

    /**
     * Returns the url link for the icon if the item.
     *
     * @param context the context
     * @return Uri object
     */
    public Uri getIconUrl(Context context) {
        String BASE_URL = "http://tlongdev.com/api/tf2_icon.php";
        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("defindex", String.valueOf(defindex));

        // TODO: 2015. 11. 09.
        boolean large = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dummy", false);
        if (large) {
            builder.appendQueryParameter("large", "1");
        }

        if (australium) {
            builder.appendQueryParameter("australium", "1");
        } else if (weaponWear >= 0) {
            builder.appendQueryParameter("wear", String.valueOf(weaponWear));
        }

        return builder.build();
    }

    /**
     * Gets the effect of the item from file
     *
     * @param context the context
     * @return icon drawable
     * @throws IOException
     */
    public Drawable getEffectDrawable(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream ims;

        //Get the icon if the effect if needed
        if (priceIndex != 0 && canHaveEffects()) {
            ims = assetManager.open("effects/" + priceIndex + "_188x188.png");
            return Drawable.createFromStream(ims, null);
        } else {
            return null;
        }
    }
}
