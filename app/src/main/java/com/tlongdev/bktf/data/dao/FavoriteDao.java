package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.tlongdev.bktf.data.entity.Favorite;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Favorite... favorites);

    @Delete
    void delete(Favorite... favorites);

    @Query("SELECT * FROM favorites " +
            "WHERE defindex = :defindex" +
            "  AND quality = :quality" +
            "  AND tradable = :tradable" +
            "  AND craftable = :craftable" +
            "  AND australium = :australium" +
            "  AND price_index = :priceIndex" +
            "  AND weapon_wear = :weaponWear")
    Favorite find(int defindex, int quality, boolean tradable, boolean craftable,
                  boolean australium, int priceIndex, int weaponWear);
}
