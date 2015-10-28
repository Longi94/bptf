package com.tlongdev.bktf.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.Utility;
import com.tlongdev.bktf.data.ItemSchemaDbHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Long on 2015. 10. 28..
 */
public class Tf2Item {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = Tf2Item.class.getSimpleName();

    private int defindex;

    private String name;

    private int quality;

    private boolean tradable;

    private boolean craftable;

    private boolean australium;

    private int priceIndex;

    private int weaponWear;

    private Price price;

    public Tf2Item() {
        this(-1, null, -1, false, false, false, -1, null);
    }

    public Tf2Item(int defindex, String name, int quality, boolean tradable, boolean craftable, boolean australium, int priceIndex, Price price) {
        this(defindex, name, quality, tradable, craftable, australium, priceIndex, -1, price);
    }

    public Tf2Item(int defindex, String name, int quality, boolean tradable, boolean craftable, boolean australium, int priceIndex, int weaponWear, Price price) {
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
            ItemSchemaDbHelper dbHelper = new ItemSchemaDbHelper(context);
            Cursor itemCursor = dbHelper.getItem(defindex);
            if (itemCursor != null && itemCursor.moveToFirst()) {
                formattedName += itemCursor.getString(0) + " " + name;
                itemCursor.close();
            }
            dbHelper.close();
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
        return formattedName + name;
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
     * This method is needed to remove all the duplicate images (~5mb freed). This method must be
     * used every time you want to load an image with its defindex.
     *
     * @return defindex to be used to load icons
     */
    public int getIconIndex() {
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
            case 1155: //wrench
                return 7;
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
            case 294: //lugermorph
                return 160;
            case 422: //companion cube pin
                return 299;
            case 681:
            case 682:
            case 683:
            case 684: //gold cup medal
                return 680;
            case 686:
            case 687:
            case 688:
            case 689: //silver cup medal
                return 685;
            case 691:
            case 692:
            case 693:
            case 694:
            case 695:
            case 696:
            case 697:
            case 698: //bronze cup medal
                return 690;
            case 736: //duplicate sapper
                return 735;
            case 737: //duplicate construction pda
                return 25;
            case 744: //pyrovision goggles
                return 743;
            case 791:
            case 928://pink promo gifts
                return 790;
            case 831:
            case 832:
            case 833:
            case 834: // promoasian items
            case 835:
            case 836:
            case 837:
            case 838:
                return defindex - 21;
            case 839: //gift bag
                return 729;
            case 850: //Minigun
                return 15;
            case 1132: //spell magazine
                return 1070;
            case 2015:
            case 2049:
            case 2079:
            case 2123:
            case 2125: //map stamps packs
            case 2138:
            case 2139:
                return 2007;
            case 2035:
            case 2036:
            case 2037:
            case 2038:
            case 2039: //dr gorbort pack
                return 2034;
            case 2041:
            case 2045:
            case 2047:
            case 2048: //deus ex pack
                return 2040;
            case 2042:
            case 2095: //soldier pack
                return 2019;
            case 2043:
            case 2044: //more soldier packs
                return 2042;
            case 2046: //shogun pack
                return 2016;
            case 2070: //promo chicken hat
                return 785;
            case 2081: //hong kong pac
                return 2076;
            case 2094:
            case 2096:
            case 2097:
            case 2098:
            case 2099:
            case 2100:
            case 2101:
            case 2102: //class packs
                return defindex - 76;
            case 2103: //deus Ex arm
                return 524;
            case 5020: //desc tag
                return 2093;
            case 5814: //scrap metal
            case 5824:
            case 5825:
                return 5000;
            case 5041:
            case 5045: //duplicate crates
                return 5022;
            case 5049:
            case 5067:
            case 5072:
            case 5073:
            case 5079:
            case 5081:
            case 5628:
            case 5631:
            case 5632:
            case 5713:
            case 5716:
            case 5717:
            case 5762:
            case 5791:
            case 5792:
                return 5021; //mann co supply crate key
            case 5074: //something special for someone special
                return 699;
            case 5601:
            case 5602:
            case 5603:
            case 5604: //pyro dusts
                return 5600;
            case 5721:
            case 5722:
            case 5723:
            case 5724:
            case 5725:
            case 5753:
            case 5754:
            case 5755:
            case 5756:
            case 5757:
            case 5758:
            case 5759:
            case 5783:
            case 5784:
            case 5804:
            case 6522: //strangifiers
                return 5661;
            case 5727:
            case 5728:
            case 5729:
            case 5730:
            case 5731:
            case 5732:
            case 5733:
            case 5743:
            case 5744:
            case 5745:
            case 5746:
            case 5747:
            case 5748:
            case 5749:
            case 5750:
            case 5751:
            case 5793:
            case 5794:
            case 5795:
            case 5796:
            case 5797:
            case 5798:
            case 5799:
            case 5800:
            case 5801:
            case 6527: //killstreak kits
                return 5726;
            case 5735:
            case 5742:
            case 5752:
            case 5781:
            case 5802:
            case 5803: //duplicate munitions
                return 5734;
            case 5738: //sombrero crate
                return 5737;
            case 5773: //halloween cauldron
                return 5772;
            case 8003:
            case 8006: //ugc medal
                return 8000;
            case 8004:
            case 8007: //ugc medal
                return 8001;
            case 8005:
            case 8008: //ugc medal
                return 8002;
            case 8012: //ugc medal
                return 8009;
            case 8013: //ugc medal
                return 8010;
            case 8014: //ugc medal
                return 8011;
            case 8018:
            case 8021: //ugc medal
                return 8015;
            case 8019:
            case 8022: //ugc medal
                return 8016;
            case 8020:
            case 8023: //ugc medal
                return 8017;
            case 8031:
            case 8035:
            case 8039:
            case 8043:
            case 8047:
            case 8051:
            case 8055:
            case 8058:
            case 8062:
            case 8066: //esl gold medal
                return 8027;
            case 8032:
            case 8036:
            case 8040:
            case 8044:
            case 8048:
            case 8052:
            case 8056:
            case 8059:
            case 8063:
            case 8067: //esl silver medal
                return 8028;
            case 8033:
            case 8037:
            case 8041:
            case 8045:
            case 8049:
            case 8053:
            case 8057:
            case 8064:
            case 8068: //esl bronze medal
                return 8029;
            case 8034:
            case 8038:
            case 8042:
            case 8046:
            case 8050:
            case 8054:
            case 8060:
            case 8061:
            case 8065:
            case 8069: //esl platinum medal
                return 8030;
            case 8071: //ready steady pan s1 medal
                return 8070;
            case 8075:
            case 8078:
            case 8081:
            case 8084:
            case 8087:
            case 8275:
            case 8307:
            case 8323:
            case 8397: //etf2l medal gold
                return 8072;
            case 8076:
            case 8079:
            case 8082:
            case 8085:
            case 8088:
            case 8276:
            case 8308:
            case 8324:
            case 8398: //etf2l medal silver
                return 8073;
            case 8077:
            case 8080:
            case 8083:
            case 8086:
            case 8089:
            case 8277:
            case 8309:
            case 8325:
            case 8399: //etf2l medal bronze
                return 8074;
            case 8091:
            case 8092:
            case 8093:
            case 8094:
            case 8095:
            case 8096:
            case 8097:
            case 8098:
            case 8099:
            case 8100:
            case 8101:
            case 8102:
            case 8103:
            case 8104:
            case 8105:
            case 8106:
            case 8107:
            case 8108:
            case 8109:
            case 8110:
            case 8111:
            case 8112:
            case 8113:
            case 8114:
            case 8115:
            case 8116:
            case 8117:
            case 8118:
            case 8119:
            case 8120:
            case 8121:
            case 8122:
            case 8123:
            case 8124:
            case 8125:
            case 8278:
            case 8279:
            case 8280:
            case 8281:
            case 8282:
            case 8283:
            case 8310:
            case 8311:
            case 8312:
            case 8313:
            case 8314:
            case 8315:
            case 8326:
            case 8327:
            case 8328:
            case 8329:
            case 8330:
            case 8331:
            case 8400:
            case 8401:
            case 8402:
            case 8403:
            case 8404: //etf2l medal participant
                return 8090;
            case 8127:
            case 8128:
            case 8129:
            case 8130:
            case 8131:
            case 8132:
            case 8133:
            case 8134:
            case 8135:
            case 8136:
            case 8137:
            case 8138:
            case 8139:
            case 8140:
            case 8141:
            case 8142:
            case 8143:
            case 8144:
            case 8145:
            case 8146:
            case 8147:
            case 8148:
            case 8149:
            case 8150:
            case 8151:
            case 8152:
            case 8153:
            case 8154:
            case 8155:
            case 8156:
            case 8157:
            case 8158:
            case 8159:
            case 8160:
            case 8161:
            case 8162:
            case 8163:
            case 8164:
            case 8165:
            case 8166:
            case 8167:
            case 8284:
            case 8285:
            case 8286:
            case 8287:
            case 8288:
            case 8289:
            case 8290:
            case 8316:
            case 8317:
            case 8318:
            case 8319:
            case 8320:
            case 8321:
            case 8322:
            case 8332:
            case 8333:
            case 8334:
            case 8335:
            case 8336:
            case 8337:
            case 8338:
            case 8406:
            case 8407:
            case 8408:
            case 8409:
            case 8410:
            case 8411:
            case 8412: //etf2l bottle cap
                return 8126;
            case 8171:
            case 8174:
            case 8291:
            case 8413: //etf2lry medal gold
                return 8168;
            case 8172:
            case 8175:
            case 8292:
            case 8414: //etf2lry medal silver
                return 8169;
            case 8173:
            case 8176:
            case 8293:
            case 8415: //etf2lry medal bronze
                return 8170;
            case 8178:
            case 8179:
            case 8180:
            case 8181:
            case 8182:
            case 8183:
            case 8184:
            case 8185:
            case 8186:
            case 8187:
            case 8188:
            case 8189:
            case 8190:
            case 8191:
            case 8192:
            case 8294:
            case 8295:
            case 8296:
            case 8297:
            case 8298:
            case 8299:
            case 8416:
            case 8417:
            case 8418:
            case 8419:
            case 8420:
            case 8421: //etf2lry medal participant
                return 8177;
            case 8194:
            case 8195:
            case 8196:
            case 8197:
            case 8198:
            case 8199:
            case 8200:
            case 8201:
            case 8202:
            case 8203:
            case 8204:
            case 8205:
            case 8206:
            case 8207:
            case 8208:
            case 8209:
            case 8210:
            case 8211:
            case 8300:
            case 8301:
            case 8302:
            case 8303:
            case 8304:
            case 8305:
            case 8306:
            case 8422:
            case 8423:
            case 8424:
            case 8425:
            case 8426:
            case 8427:
            case 8428: //etf2lry bottle cap
                return 8193;
            case 8213:
            case 8214:
            case 8215: //soldier medal
            case 8775:
            case 8776:
            case 8777:
            case 8778:
                return 8212;
            case 8217:
            case 8218:
            case 8219:
            case 8220:
            case 8221:
            case 8222: //soldier medal2
                return 8216;
            case 8223: //duplicate soldier medal
                return 121;
            case 8236: //horseshoe medal 1st
                return 8224;
            case 8237: //horseshoe medal 2st
                return 8225;
            case 8238: //horseshoe medal 3st
                return 8226;
            case 8244:
            case 8245:
            case 8246:
            case 8247: //ready steady pan s2
                return 8243;
            case 8263: //wings medal first
                return 8248;
            case 8264: //wings medal second
                return 8249;
            case 8265: //wings medal third
                return 8250;
            case 8271: //ugc wings gold
                return 8267;
            case 8272:
            case 8274: //ugc wings silver
                return 8268;
            case 8273: //ugc wings bronze
                return 8269;
            case 8457:
            case 8650: //grey shield first
            case 8780:
            case 8839:
                return 8339;
            case 8458:
            case 8651: //grey shield second
            case 8781:
            case 8840:
                return 8340;
            case 8459:
            case 8652: //grey shield third
            case 8782:
            case 8841:
                return 8341;
            case 8460:
            case 8653: //grey shield participant
            case 8783:
            case 8842:
                return 8342;
            case 8347:
            case 8351:
            case 8355:
            case 8359:
            case 8461:
            case 8465:
            case 8469:
            case 8473:
            case 8654:
            case 8658:
            case 8662:
            case 8666: //gold shield first
            case 8784:
            case 8788:
            case 8792:
            case 8796:
            case 8843:
            case 8847:
            case 8851:
            case 8855:
                return 8343;
            case 8348:
            case 8352:
            case 8356:
            case 8360:
            case 8462:
            case 8466:
            case 8470:
            case 8474:
            case 8655:
            case 8659:
            case 8663:
            case 8667: //gold shield second
            case 8785:
            case 8789:
            case 8793:
            case 8797:
            case 8844:
            case 8848:
            case 8852:
            case 8856:
                return 8344;
            case 8349:
            case 8353:
            case 8357:
            case 8361:
            case 8463:
            case 8467:
            case 8471:
            case 8475:
            case 8656:
            case 8660:
            case 8664:
            case 8668: //gold shield third
            case 8786:
            case 8790:
            case 8794:
            case 8798:
            case 8845:
            case 8849:
            case 8853:
            case 8857:
                return 8345;
            case 8350:
            case 8354:
            case 8358:
            case 8362:
            case 8464:
            case 8468:
            case 8472:
            case 8476:
            case 8657:
            case 8661:
            case 8665:
            case 8669: //gold shield participant
            case 8787:
            case 8791:
            case 8795:
            case 8799:
            case 8846:
            case 8850:
            case 8854:
            case 8858:
                return 8346;
            case 8388: //silver shooting star first
                return 8368;
            case 8389: //silver shooting star first
                return 8369;
            case 8390: //silver shooting star first
                return 8370;
            case 8391: //silver shooting star first
                return 8371;
            case 8392: //shooting star metal
                return 8379;
            case 8393: //shooting star dark metal
                return 8383;
            case 8394: //shooting star darkest metal
                return 8387;
            case 8433:
            case 8437:
            case 8441: //africa gold
                return 8429;
            case 8434:
            case 8438:
            case 8442: //africa silver
                return 8430;
            case 8435:
            case 8439:
            case 8443: //africa bronze
                return 8431;
            case 8436:
            case 8440:
            case 8444: //africa green
                return 8432;
            case 8446:
            case 8447: //tiger gold
                return 8445;
            case 8450:
            case 8451: //tiger silver
                return 8449;
            case 8452:
            case 8456: //snake black
                return 8448;
            case 8454:
            case 8455: //tiger gold
                return 8453;
            case 8492:
            case 8675:
            case 8689:
            case 8704:
            case 8718://Fiery medal 1
                return 8478;
            case 8493:
            case 8676:
            case 8690:
            case 8705:
            case 8719: //Fiery medal 2
                return 8479;
            case 8494:
            case 8677:
            case 8691:
            case 8706:
            case 8720: //Fiery medal 3
                return 8480;
            case 8495:
            case 8678:
            case 8692:
            case 8707:
            case 8721: //Fiery medal star
                return 8481;
            case 8679:
            case 8697:
            case 8708:
            case 8726: //gold eagle medal 1
                return 8482;
            case 8680:
            case 8698:
            case 8709:
            case 8727: //gold eagle medal 2
                return 8483;
            case 8681:
            case 8699:
            case 8710:
            case 8728: //gold eagle medal 3
                return 8484;
            case 8496:
            case 8682:
            case 8693:
            case 8700:
            case 8711:
            case 8722:
            case 8729: //gold eagle medal star
                return 8485;
            case 8683:
            case 8712: //silver eagle medal 1
                return 8486;
            case 8684:
            case 8713: //silver eagle medal 2
                return 8487;
            case 8685:
            case 8714: //silver eagle medal 3
                return 8488;
            case 8497:
            case 8500:
            case 8686:
            case 8694:
            case 8701:
            case 8715:
            case 8723:
            case 8730: //silver eagle medal star
                return 8489;
            case 8498:
            case 8501:
            case 8687:
            case 8695:
            case 8702:
            case 8716:
            case 8724:
            case 8731: //iron eagle medal star
                return 8490;
            case 8499:
            case 8688:
            case 8696:
            case 8703:
            case 8717:
            case 8725:
            case 8732: //rust eagle medal star
                return 8490;
            case 8534: //tf2 medal gold
            case 8737:
            case 8741:
            case 8746:
            case 8751:
            case 8812:
            case 8816:
            case 8821:
            case 8826:
                return 8502;
            case 8535: //tf2 medal silver
            case 8738:
            case 8742:
            case 8747:
            case 8752:
            case 8813:
            case 8817:
            case 8822:
            case 8827:
                return 8503;
            case 8536: //tf2 medal bronze
            case 8739:
            case 8743:
            case 8748:
            case 8753:
            case 8814:
            case 8818:
            case 8823:
            case 8828:
                return 8504;
            case 8506:
            case 8507:
            case 8508:
            case 8509:
            case 8510:
            case 8537:
            case 8538:
            case 8539:
            case 8540:
            case 8541:
            case 8542: //tf2 medal star
            case 8744:
            case 8749:
            case 8754:
            case 8819:
            case 8824:
            case 8829:
                return 8505;
            case 8512:
            case 8513:
            case 8514:
            case 8515:
            case 8516:
            case 8517:
            case 8543:
            case 8544:
            case 8545:
            case 8546:
            case 8547:
            case 8548:
            case 8549: //tf2 medal participant
            case 8740:
            case 8745:
            case 8750:
            case 8755:
            case 8815:
            case 8820:
            case 8825:
            case 8830:
                return 8511;
            case 8550: //tf2 medal gold_
            case 8756:
            case 8760:
            case 8765:
            case 8770:
                return 8518;
            case 8551: //tf2 medal silver_
            case 8757:
            case 8761:
            case 8766:
            case 8771:
                return 8519;
            case 8552: //tf2 medal bronze_
            case 8758:
            case 8762:
            case 8767:
            case 8772:
                return 8520;
            case 8522:
            case 8523:
            case 8524:
            case 8525:
            case 8526:
            case 8553:
            case 8554:
            case 8555:
            case 8556:
            case 8557:
            case 8558: //tf2 medal star_
            case 8763:
            case 8768:
            case 8773:
                return 8521;
            case 8528:
            case 8529:
            case 8530:
            case 8531:
            case 8532:
            case 8533:
            case 8559:
            case 8560:
            case 8561:
            case 8562:
            case 8563:
            case 8564:
            case 8565: //tf2 medal participant_
            case 8759:
            case 8764:
            case 8769:
            case 8774:
                return 8527;
            case 8568:
            case 8570:
            case 8572: //bear medal
                return 8566;
            case 8569:
            case 8571:
            case 8573:
            case 8574: //bear paw
                return 8567;
            case 8589:
            case 8593:
            case 8597:
            case 8733://esl like medal gold
                return 8585;
            case 8590:
            case 8594:
            case 8598:
            case 8734: //esl like medal silver
                return 8586;
            case 8591:
            case 8595:
            case 8599:
            case 8735: //esl like medal bronze
                return 8587;
            case 8592:
            case 8596:
            case 8600:
            case 8736://esl like badge grey
                return 8588;
            case 8609:
            case 8613:
            case 8617:
            case 8621:
            case 8625:
            case 8629: //olympic medal gold
            case 8868:
            case 8872:
            case 8876:
            case 8880:
                return 8605;
            case 8610:
            case 8614:
            case 8618:
            case 8622:
            case 8626:
            case 8630: //olympic medal silver
            case 8869:
            case 8873:
            case 8877:
            case 8881:
                return 8606;
            case 8611:
            case 8615:
            case 8619:
            case 8623:
            case 8627:
            case 8631: //olympic medal bronze
            case 8870:
            case 8874:
            case 8878:
            case 8882:
                return 8607;
            case 8612:
            case 8616:
            case 8620:
            case 8624:
            case 8628:
            case 8632: //olympic medal participant
            case 8871:
            case 8875:
            case 8879:
            case 8883:
                return 8608;
            case 8638:
            case 8642:
            case 8646: //hexagon medal gold
            case 8800:
            case 8804:
            case 8808:
                return 8634;
            case 8639:
            case 8643:
            case 8647: //hexagon medal silver
            case 8801:
            case 8805:
            case 8809:
                return 8635;
            case 8640:
            case 8644:
            case 8648: //hexagon medal bronze
            case 8802:
            case 8806:
            case 8810:
                return 8636;
            case 8641:
            case 8645:
            case 8649: //hexagon medal participant
            case 8803:
            case 8807:
            case 8811:
                return 8637;
            case 8835: //bullet gold
                return 8831;
            case 8836: //bullet silver
                return 8832;
            case 8837: //bullet bronze
                return 8833;
            case 8838: //bullet participant
                return 8834;
            case 8884: //fbtf gold
                return 8864;
            case 8885: //fbtf silver
                return 8865;
            case 8886: //fbtf bronze
                return 8866;
            case 8887: //fbtf participant
                return 8867;
            case 8901:
            case 8902:
            case 8903:
            case 8904: //spell exorcism
                return 8900;
            case 8906:
            case 8907:
            case 8908:
            case 8909:
            case 8910:
            case 8911:
            case 8912:
            case 8913: //spell voices
                return 8905;
            case 8915:
            case 8916:
            case 8917:
            case 8918:
            case 8919:
            case 8920://spell spectrum paint
                return 8914;
            case 8923:
            case 8924: //spell pumpkin projectiles
                return 8922;
            case 8936: //spell book page
                return 8935;
            case 20001:
            case 20005:
            case 20006:
            case 20007:
            case 20008:
            case 20009://chemistry sets
                return 20000;
            case 25001:
            case 25002:
            case 25003:
            case 25004:
            case 25005:
            case 25006:
            case 25007:
            case 25008:
            case 25009:
            case 25010:
            case 25011:
            case 25012:
            case 25013:
            case 25014://quest thingie
                return 25000;
            case 30144:
            case 30145:
            case 30146:
            case 30147:
            case 30149:
            case 30150:
            case 30151:
            case 30152:
            case 30153:
            case 30154:
            case 30155:
            case 30156:
            case 30157:
            case 30158:
            case 30159:
            case 30160:
            case 30161: //white circles
                return 30143;
            default: //don't change
                return defindex;
        }
    }

    public Drawable getIconDrawable(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream ims;

        //Get the icon of the item
        if (australium && defindex != 5037) {
            ims = assetManager.open("items/" + getIconIndex() + "aus.png");
        } else {
            ims = assetManager.open("items/" + getIconIndex() + ".png");
        }
        return Drawable.createFromStream(ims, null);
    }

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
