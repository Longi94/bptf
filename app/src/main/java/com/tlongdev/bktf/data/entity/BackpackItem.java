package com.tlongdev.bktf.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@SuppressWarnings("NullableProblems")
@Entity(tableName = "backpack")
public class BackpackItem {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private Long id;

    @ColumnInfo(name = "position")
    @NonNull
    private Integer position;

    @ColumnInfo(name = "unique_id")
    @NonNull
    private Long uniqueId = 0L;

    @ColumnInfo(name = "original_id")
    @NonNull
    private Long originalId = 0L;

    @ColumnInfo(name = "defindex")
    @NonNull
    private Integer defindex = 0;

    @ColumnInfo(name = "level")
    @NonNull
    private Integer level = 0;

    @ColumnInfo(name = "origin")
    @NonNull
    private Integer origin = 0;

    @ColumnInfo(name = "flag_cannot_trade")
    @NonNull
    private Boolean flagCannotTrade = false;

    @ColumnInfo(name = "flag_cannot_craft")
    @NonNull
    private Boolean flagCannotCraft = false;

    @ColumnInfo(name = "quality")
    @NonNull
    private Integer quality = 0;

    @ColumnInfo(name = "custom_name")
    private String customName;

    @ColumnInfo(name = "custom_description")
    private String customDescription;

    @ColumnInfo(name = "equipped")
    @NonNull
    private Boolean equipped = false;

    @ColumnInfo(name = "item_index")
    @NonNull
    private Integer itemIndex = 0;

    @ColumnInfo(name = "paint")
    private Integer paint;

    @ColumnInfo(name = "craft_number")
    private Integer craftNumber;

    @ColumnInfo(name = "creator_name")
    private String creatorName;

    @ColumnInfo(name = "gifter_name")
    private String gifterName;

    @ColumnInfo(name = "contained_item")
    private String containedItem;

    @ColumnInfo(name = "australium")
    @NonNull
    private Boolean australium = false;

    @ColumnInfo(name = "weapon_wear")
    private Integer weaponWear;

    @ColumnInfo(name = "guest")
    @NonNull
    private Boolean guest;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NonNull
    public Integer getPosition() {
        return position;
    }

    public void setPosition(@NonNull Integer position) {
        this.position = position;
    }

    @NonNull
    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(@NonNull Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    @NonNull
    public Long getOriginalId() {
        return originalId;
    }

    public void setOriginalId(@NonNull Long originalId) {
        this.originalId = originalId;
    }

    @NonNull
    public Integer getDefindex() {
        return defindex;
    }

    public void setDefindex(@NonNull Integer defindex) {
        this.defindex = defindex;
    }

    @NonNull
    public Integer getLevel() {
        return level;
    }

    public void setLevel(@NonNull Integer level) {
        this.level = level;
    }

    @NonNull
    public Integer getOrigin() {
        return origin;
    }

    public void setOrigin(@NonNull Integer origin) {
        this.origin = origin;
    }

    @NonNull
    public Boolean getFlagCannotTrade() {
        return flagCannotTrade;
    }

    public void setFlagCannotTrade(@NonNull Boolean flagCannotTrade) {
        this.flagCannotTrade = flagCannotTrade;
    }

    @NonNull
    public Boolean getFlagCannotCraft() {
        return flagCannotCraft;
    }

    public void setFlagCannotCraft(@NonNull Boolean flagCannotCraft) {
        this.flagCannotCraft = flagCannotCraft;
    }

    @NonNull
    public Integer getQuality() {
        return quality;
    }

    public void setQuality(@NonNull Integer quality) {
        this.quality = quality;
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

    @NonNull
    public Boolean getEquipped() {
        return equipped;
    }

    public void setEquipped(@NonNull Boolean equipped) {
        this.equipped = equipped;
    }

    @NonNull
    public Integer getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(@NonNull Integer itemIndex) {
        this.itemIndex = itemIndex;
    }

    public Integer getPaint() {
        return paint;
    }

    public void setPaint(Integer paint) {
        this.paint = paint;
    }

    public Integer getCraftNumber() {
        return craftNumber;
    }

    public void setCraftNumber(Integer craftNumber) {
        this.craftNumber = craftNumber;
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

    @NonNull
    public Boolean getAustralium() {
        return australium;
    }

    public void setAustralium(@NonNull Boolean australium) {
        this.australium = australium;
    }

    public Integer getWeaponWear() {
        return weaponWear;
    }

    public void setWeaponWear(Integer weaponWear) {
        this.weaponWear = weaponWear;
    }

    @NonNull
    public Boolean getGuest() {
        return guest;
    }

    public void setGuest(@NonNull Boolean guest) {
        this.guest = guest;
    }
}
