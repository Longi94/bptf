package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.tlongdev.bktf.data.entity.Favorite;

/**
 * Created by lngtr on 2018-01-18.
 */
@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(Favorite favorite);

    @Delete
    void deleteFavorite(Favorite favorite);

    @Query("SELECT * FROM favorites " +
            "WHERE defindex = :defindex" +
            "  AND quality = :quality" +
            "  AND tradable = :tradable" +
            "  AND craftable = :craftable" +
            "  AND australium = :australium" +
            "  AND price_index = :priceIndex" +
            "  AND weapon_wear = :weaponWear")
    Favorite findFavorite(int defindex, int quality, boolean tradable, boolean craftable,
                    boolean australium, int priceIndex, int weaponWear);
}
