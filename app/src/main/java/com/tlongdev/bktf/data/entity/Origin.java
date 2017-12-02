package com.tlongdev.bktf.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by lngtr on 2017-12-02.
 */
@Entity(tableName = "origin_names")
public class Origin {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private long id;

    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    public Origin() {
    }

    @Ignore
    public Origin(long id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}
