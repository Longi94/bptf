package com.tlongdev.bktf.model;

import android.content.Context;

import com.tlongdev.bktf.R;

/**
 * Created by Long on 2015. 10. 31..
 */
public class BackpackItem extends Item {

    private int uniqueId;
    private int originalId;
    private int level;
    private int origin;
    private int paint;
    private int craftNumber;

    private String customName;
    private String customDescription;;
    private String creatorName;
    private String gifterName;
    private String containedItem;

    private boolean equipped;

    public BackpackItem() {
        this(0, null, 0, false, false, false, 0, null, 0, 0, 0, 0, 0, 0, null, null, null, null, null, false);
    }

    public BackpackItem(int defindex, String name, int quality, boolean tradable, boolean craftable,
                        boolean australium, int priceIndex, Price price, int uniqueId, int originalId,
                        int level, int origin, int paint, int craftNumber, String customName,
                        String customDescription, String creatorName, String gifterName, String containedItem, boolean equipped) {
        this(defindex, name, quality, tradable, craftable, australium, priceIndex, 0, price, uniqueId,
                originalId, level, origin, paint, craftNumber, customName, customDescription, creatorName,
                gifterName, containedItem, equipped);
    }

    public BackpackItem(int defindex, String name, int quality, boolean tradable, boolean craftable,
                        boolean australium, int priceIndex, int weaponWear, Price price, int uniqueId,
                        int originalId, int level, int origin, int paint, int craftNumber, String customName,
                        String customDescription, String creatorName, String gifterName, String containedItem,
                        boolean equipped) {
        super(defindex, name, quality, tradable, craftable, australium, priceIndex, weaponWear, price);
        this.uniqueId = uniqueId;
        this.originalId = originalId;
        this.level = level;
        this.origin = origin;
        this.paint = paint;
        this.craftNumber = craftNumber;
        this.customName = customName;
        this.customDescription = customDescription;
        this.creatorName = creatorName;
        this.gifterName = gifterName;
        this.containedItem = containedItem;
        this.equipped = equipped;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getOriginalId() {
        return originalId;
    }

    public void setOriginalId(int originalId) {
        this.originalId = originalId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getPaint() {
        return paint;
    }

    public void setPaint(int paint) {
        this.paint = paint;
    }

    public int getCraftNumber() {
        return craftNumber;
    }

    public void setCraftNumber(int craftNumber) {
        this.craftNumber = craftNumber;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCustomDescription() {
        return customDescription;
    }

    public void setCustomDescription(String customDescription) {
        this.customDescription = customDescription;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getGifterName() {
        return gifterName;
    }

    public void setGifterName(String gifterName) {
        this.gifterName = gifterName;
    }

    public String getContainedItem() {
        return containedItem;
    }

    public void setContainedItem(String containedItem) {
        this.containedItem = containedItem;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }

    /**
     * Get the paint name according to the index returned by the GetPlayerItems API (attributes).
     *
     * @return paint name
     */
    public String getPaintName(Context context) {
        switch (paint) {
            case 7511618:
                return context.getString(R.string.paint_green);
            case 4345659:
                return context.getString(R.string.paint_greed);
            case 5322826:
                return context.getString(R.string.paint_violet);
            case 14204632:
                return context.getString(R.string.paint_216);
            case 8208497:
                return context.getString(R.string.paint_purple);
            case 13595446:
                return context.getString(R.string.paint_orange);
            case 10843461:
                return context.getString(R.string.paint_braun);
            case 12955537:
                return context.getString(R.string.paint_drab);
            case 6901050:
                return context.getString(R.string.paint_brown);
            case 8154199:
                return context.getString(R.string.paint_rustic);
            case 15185211:
                return context.getString(R.string.paint_australium);
            case 8289918:
                return context.getString(R.string.paint_grey);
            case 15132390:
                return context.getString(R.string.paint_white);
            case 1315860:
                return context.getString(R.string.paint_black);
            case 16738740:
                return context.getString(R.string.paint_pink);
            case 3100495:
                return context.getString(R.string.paint_slate);
            case 8421376:
                return context.getString(R.string.paint_olive);
            case 3329330:
                return context.getString(R.string.paint_lime);
            case 15787660:
                return context.getString(R.string.paint_business);
            case 15308410:
                return context.getString(R.string.paint_salmon);
            case 12073019:
                return context.getString(R.string.paint_team);
            case 4732984:
                return context.getString(R.string.paint_overalls);
            case 11049612:
                return context.getString(R.string.paint_lab);
            case 3874595:
                return context.getString(R.string.paint_balaclava);
            case 6637376:
                return context.getString(R.string.paint_dabonair);
            case 8400928:
                return context.getString(R.string.paint_teamwork);
            case 12807213:
                return context.getString(R.string.paint_cream);
            case 2960676:
                return context.getString(R.string.paint_eight);
            case 12377523:
                return context.getString(R.string.paint_mint);
            default:
                return null;
        }
    }

    /**
     * Check whether the index is actually a paint.
     *
     * @param index index returned by the api
     * @return true if index is paint
     */
    public static boolean isPaint(int index) {
        switch (index) {
            case 7511618:
            case 4345659:
            case 5322826:
            case 14204632:
            case 8208497:
            case 13595446:
            case 10843461:
            case 12955537:
            case 6901050:
            case 8154199:
            case 15185211:
            case 8289918:
            case 15132390:
            case 1315860:
            case 16738740:
            case 3100495:
            case 8421376:
            case 3329330:
            case 15787660:
            case 15308410:
            case 12073019:
            case 4732984:
            case 11049612:
            case 3874595:
            case 6637376:
            case 8400928:
            case 12807213:
            case 2960676:
            case 12377523:
                return true;
            default:
                return false;
        }
    }
}
