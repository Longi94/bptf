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

package com.tlongdev.bktf.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.data.DatabaseContract;
import com.tlongdev.bktf.data.DatabaseContract.DecoratedWeaponEntry;
import com.tlongdev.bktf.data.DatabaseContract.ItemSchemaEntry;
import com.tlongdev.bktf.util.Utility;

import java.util.Locale;

/**
 * Item class
 */
public class Item implements Parcelable {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = Item.class.getSimpleName();

    private int defindex;

    private String name;

    @Quality.Enum
    private int quality;

    private boolean tradable;

    private boolean craftable;

    private boolean australium;

    private int priceIndex;

    private int weaponWear;

    private Price price;

    public static final Parcelable.Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    protected Item(Parcel source) {
        defindex = source.readInt();
        name = source.readString();
        //noinspection WrongConstant
        quality = source.readInt();
        tradable = source.readByte() != 0;
        craftable = source.readByte() != 0;
        australium = source.readByte() != 0;
        priceIndex = source.readInt();
        weaponWear = source.readInt();
        price = source.readParcelable(Price.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(defindex);
        dest.writeString(name);
        dest.writeInt(quality);
        dest.writeByte((byte) (tradable ? 1 : 0));
        dest.writeByte((byte) (craftable ? 1 : 0));
        dest.writeByte((byte) (australium ? 1 : 0));
        dest.writeInt(priceIndex);
        dest.writeInt(weaponWear);
        dest.writeParcelable(price, flags);
    }

    public Item() {}

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

    @Quality.Enum
    public int getQuality() {
        return quality;
    }

    public void setQuality( @Quality.Enum int quality) {
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
    @SuppressLint("SwitchIntDef")
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
                    new String[]{String.valueOf(priceIndex)},
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
            case Quality.PAINTKITWEAPON:
                break;
            default:
                formattedName += context.getString(R.string.quality_normal) + " ";
                break;
        }

        if (australium) {
            formattedName += "Australium ";
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
        //Unusuals, self-made and community items
        if (quality == Quality.UNUSUAL || quality == Quality.COMMUNITY || quality == Quality.SELF_MADE) {
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
    @SuppressLint("SwitchIntDef")
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
        Cursor cursor = context.getContentResolver().query(
                DecoratedWeaponEntry.CONTENT_URI,
                new String[]{DecoratedWeaponEntry._ID, DecoratedWeaponEntry.COLUMN_GRADE},
                DecoratedWeaponEntry.COLUMN_DEFINDEX + " = ?",
                new String[]{String.valueOf(defindex)},
                null
        );

        int colorResource = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int grade = cursor.getInt(1);
                switch (grade) {
                    case 0:
                        colorResource = isDark ? R.color.tf2_decorated_weapon_civilian_dark : R.color.tf2_decorated_weapon_civilian;
                        break;
                    case 1:
                        colorResource = isDark ? R.color.tf2_decorated_weapon_freelance_dark : R.color.tf2_decorated_weapon_freelance;
                        break;
                    case 2:
                        colorResource = isDark ? R.color.tf2_decorated_weapon_mercenary_dark : R.color.tf2_decorated_weapon_mercenary;
                        break;
                    case 3:
                        colorResource = isDark ? R.color.tf2_decorated_weapon_commando_dark : R.color.tf2_decorated_weapon_commando;
                        break;
                    case 4:
                        colorResource = isDark ? R.color.tf2_decorated_weapon_assassin_dark : R.color.tf2_decorated_weapon_assassin;
                        break;
                    case 5:
                        colorResource = isDark ? R.color.tf2_decorated_weapon_elite_dark : R.color.tf2_decorated_weapon_elite;
                        break;
                }
            }
            cursor.close();
        }
        return colorResource == 0 ? Utility.getColor(context, R.color.tf2_normal_color) : Utility.getColor(context, colorResource);
    }

    /**
     * Gets the decorated weapon description.
     *
     * @param type the type of the decorated weapon
     * @return the formatted description string
     */
    public String getDecoratedWeaponDesc(Context context, String type) {
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

        Cursor cursor = context.getContentResolver().query(
                DecoratedWeaponEntry.CONTENT_URI,
                new String[]{DecoratedWeaponEntry._ID, DecoratedWeaponEntry.COLUMN_GRADE},
                DecoratedWeaponEntry.COLUMN_DEFINDEX + " = ?",
                new String[]{String.valueOf(defindex)},
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int grade = cursor.getInt(1);
                switch (grade) {
                    case 0:
                        return "Civilian Grade " + type + " " + wearStr;
                    case 1:
                        return "Freelance Grade " + type + " " + wearStr;
                    case 2:
                        return "Mercenary Grade " + type + " " + wearStr;
                    case 3:
                        return "Commando Grade " + type + " " + wearStr;
                    case 4:
                        return "Assassin Grade " + type + " " + wearStr;
                    case 5:
                        return "Elite Grade " + type + " " + wearStr;
                    default:
                        throw new IllegalArgumentException("Invalid defindex: " + defindex);
                }
            }
            cursor.close();
        }

        return type + " " + wearStr;
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
     * @return Uri object
     */
    public String getIconUrl() {
        if (australium) {
            return "file:///android_asset/australium/" + defindex + ".png";
        } else if (weaponWear > 0) {
            return "file:///android_asset/decorated/" + defindex + "/" + weaponWear + ".png";
        }
        return "file:///android_asset/items/" + defindex + ".png";
    }

    /**
     * Returns the url link for the effect if the item.
     *
     * @return Uri object
     */
    public String getEffectUrl() {
        return "file:///android_asset/effect/" + priceIndex + ".png";
    }

    public String getBackpackTfUrl() {
        String url;
        if (!australium) {
            url = String.format(Locale.ENGLISH, "http://backpack.tf/stats/%d/%d/%d/%d", quality, defindex, tradable ? 1 : 0, craftable ? 1 : 0);
        } else {
            url = String.format(Locale.ENGLISH, "http://backpack.tf/stats/%d/%s/%d/%d", quality, "Australium " + name, tradable ? 1 : 0, craftable ? 1 : 0);
        }

        return priceIndex > 0 ? url + "/" + priceIndex : url;
    }
}
