package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.tlongdev.bktf.data.entity.DecoratedWeapon;

import java.util.Collection;

/**
 * Created by lngtr on 2017-12-02.
 */
@Dao
public interface DecoratedWeaponDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDecoratedWeapons(Collection<DecoratedWeapon> origins);

    @Query("SELECT * FROM decorated_weapons WHERE defindex = :defindex")
    DecoratedWeapon getDecoratedWeapon(int defindex);
}
