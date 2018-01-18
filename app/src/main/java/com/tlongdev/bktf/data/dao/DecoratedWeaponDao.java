package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.tlongdev.bktf.data.entity.DecoratedWeapon;

import java.util.Collection;

@Dao
public interface DecoratedWeaponDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Collection<DecoratedWeapon> origins);

    @Query("SELECT * FROM decorated_weapons WHERE defindex = :defindex")
    DecoratedWeapon find(int defindex);
}
