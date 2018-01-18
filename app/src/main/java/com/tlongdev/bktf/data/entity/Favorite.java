package com.tlongdev.bktf.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by lngtr on 2018-01-18.
 */
@SuppressWarnings("NullableProblems")
@Entity(tableName = "favorites", indices = @Index(value = {
        "defindex", "quality", "tradable", "craftable", "price_index", "australium", "weapon_wear"
}, unique = true))
public class Favorite {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long id;

    @ColumnInfo(name = "defindex")
    @NonNull
    private Integer defindex;

    @ColumnInfo(name = "quality")
    @NonNull
    private Integer quality;

    @ColumnInfo(name = "tradable")
    @NonNull
    private Boolean tradable;

    @ColumnInfo(name = "craftable")
    @NonNull
    private Boolean craftable;

    @ColumnInfo(name = "price_index")
    @NonNull
    private Integer priceIndex;

    @ColumnInfo(name = "australium")
    @NonNull
    private Boolean australium;

    @ColumnInfo(name = "weapon_wear")
    @NonNull
    private Integer weaponWear;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public Integer getDefindex() {
        return defindex;
    }

    public void setDefindex(@NonNull Integer defindex) {
        this.defindex = defindex;
    }

    @NonNull
    public Integer getQuality() {
        return quality;
    }

    public void setQuality(@NonNull Integer quality) {
        this.quality = quality;
    }

    @NonNull
    public Boolean getTradable() {
        return tradable;
    }

    public void setTradable(@NonNull Boolean tradable) {
        this.tradable = tradable;
    }

    @NonNull
    public Boolean getCraftable() {
        return craftable;
    }

    public void setCraftable(@NonNull Boolean craftable) {
        this.craftable = craftable;
    }

    @NonNull
    public Integer getPriceIndex() {
        return priceIndex;
    }

    public void setPriceIndex(@NonNull Integer priceIndex) {
        this.priceIndex = priceIndex;
    }

    @NonNull
    public Boolean getAustralium() {
        return australium;
    }

    public void setAustralium(@NonNull Boolean australium) {
        this.australium = australium;
    }

    @NonNull
    public Integer getWeaponWear() {
        return weaponWear;
    }

    public void setWeaponWear(@NonNull Integer weaponWear) {
        this.weaponWear = weaponWear;
    }
}
