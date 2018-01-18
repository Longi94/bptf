package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.tlongdev.bktf.data.entity.UnusualSchema;

import java.util.Collection;

@Dao
public interface UnusualSchemaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Collection<UnusualSchema> schemas);

    @Query("SELECT * FROM unusual_schema")
    UnusualSchema[] findAll();

    @Query("SELECT * FROM unusual_schema WHERE _id = :id")
    UnusualSchema find(int id);
}
