package com.tlongdev.bktf.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by lngtr on 2017-12-02.
 */
@Entity(tableName = "decorated_weapons", indices = @Index(value = "defindex", unique = true))
public class DecoratedWeapon {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private long id;

    @ColumnInfo(name = "defindex")
    private int defindex;

    @ColumnInfo(name = "grade")
    private int grade;

    public DecoratedWeapon() {
    }

    @Ignore
    public DecoratedWeapon(int defindex, int grade) {
        this.defindex = defindex;
        this.grade = grade;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDefindex() {
        return defindex;
    }

    public void setDefindex(int defindex) {
        this.defindex = defindex;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }
}
