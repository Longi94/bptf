package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.tlongdev.bktf.data.entity.BackpackItem;

import java.util.List;

@Dao
public interface BackpackDao {

    @Query("SELECT * FROM backpack WHERE guest = :isGuest ORDER BY position ASC")
    List<BackpackItem> findAll(boolean isGuest);

    @Query("SELECT * FROM backpack WHERE _id = :id AND guest = :isGuest")
    BackpackItem find(long id, boolean isGuest);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<BackpackItem> backpackItems);

    @Query("DELETE FROM backpack WHERE guest = :isGuest")
    void deleteAll(boolean isGuest);
}
