package com.tlongdev.bktf.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.tlongdev.bktf.model.Currency;

/**
 * Created by lngtr on 2017-12-04.
 */
@SuppressWarnings("NullableProblems")
@Entity(tableName = "pricelist", indices = @Index(value = {
        "defindex", "quality", "tradable", "craftable", "price_index", "australium", "weapon_wear"
}, unique = true))
public class Price {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private Long id;

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

    @ColumnInfo(name = "price")
    @NonNull
    private Double value;

    @ColumnInfo(name = "max")
    private Double highValue;

    @ColumnInfo(name = "last_update")
    @NonNull
    private Long lastUpdate;

    @ColumnInfo(name = "difference")
    @NonNull
    private Double difference;

    @Currency.Enum
    @ColumnInfo(name = "currency")
    @NonNull
    private String currency;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    @NonNull
    public Double getValue() {
        return value;
    }

    public void setValue(@NonNull Double value) {
        this.value = value;
    }

    public Double getHighValue() {
        return highValue;
    }

    public void setHighValue(Double highValue) {
        this.highValue = highValue;
    }

    public Double getRawValue() {
        // TODO: 2017-12-04
        return 0.0;
    }

    @NonNull
    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(@NonNull Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @NonNull
    public Double getDifference() {
        return difference;
    }

    public void setDifference(@NonNull Double difference) {
        this.difference = difference;
    }

    @NonNull
    @Currency.Enum
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(@NonNull @Currency.Enum String currency) {
        this.currency = currency;
    }
}
