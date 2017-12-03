package com.tlongdev.bktf.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by lngtr on 2017-12-02.
 */
@SuppressWarnings("NullableProblems")
@Entity(tableName = "item_schema")
public class ItemSchema {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private long id;

    @ColumnInfo(name = "defindex")
    @NonNull
    private Integer defindex;

    @ColumnInfo(name = "item_name")
    @NonNull
    private String itemName;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "type_name")
    @NonNull
    private String typeName;

    @ColumnInfo(name = "proper_name")
    @NonNull
    private Boolean properName;

    public ItemSchema() {
    }

    @Ignore
    public ItemSchema(@NonNull Integer defindex, @NonNull String itemName, String description, @NonNull String typeName, @NonNull Boolean properName) {
        this.defindex = defindex;
        this.itemName = itemName;
        this.description = description;
        this.typeName = typeName;
        this.properName = properName;
    }

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
    public String getItemName() {
        return itemName;
    }

    public void setItemName(@NonNull String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(@NonNull String typeName) {
        this.typeName = typeName;
    }

    @NonNull
    public Boolean getProperName() {
        return properName;
    }

    public void setProperName(@NonNull Boolean properName) {
        this.properName = properName;
    }
}
