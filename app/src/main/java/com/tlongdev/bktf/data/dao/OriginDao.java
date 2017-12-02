package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.tlongdev.bktf.data.entity.Origin;

import java.util.Collection;

/**
 * Created by lngtr on 2017-12-02.
 */
@Dao
public interface OriginDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrigins(Collection<Origin> origins);

    @Query("SELECT * FROM origin_names WHERE _id = :id")
    Origin getOrigin(int id);
}
