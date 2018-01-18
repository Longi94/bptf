package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.tlongdev.bktf.data.entity.Origin;

import java.util.Collection;

@Dao
public interface OriginDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Collection<Origin> origins);

    @Query("SELECT * FROM origin_names WHERE _id = :id")
    Origin find(int id);
}
