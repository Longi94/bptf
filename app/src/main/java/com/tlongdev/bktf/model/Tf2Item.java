package com.tlongdev.bktf.model;

/**
 * Created by Long on 2015. 10. 28..
 */
public class Tf2Item {

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
}
