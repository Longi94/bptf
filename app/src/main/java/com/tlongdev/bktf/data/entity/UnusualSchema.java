package com.tlongdev.bktf.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by lngtr on 2017-11-28.
 */
@SuppressWarnings("NullableProblems")
@Entity(tableName = "unusual_schema")
public class UnusualSchema {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private long id;

    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    public UnusualSchema() {
    }

    @Ignore
    public UnusualSchema(long id, @NonNull String name) {
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
