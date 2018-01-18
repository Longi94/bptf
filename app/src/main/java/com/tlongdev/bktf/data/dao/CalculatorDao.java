package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.tlongdev.bktf.data.entity.CalculatorItem;

@Dao
public interface CalculatorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CalculatorItem... calculatorItems);

    @Delete
    void delete(CalculatorItem... calculatorItems);

    @Query("SELECT * FROM calculator " +
            "WHERE defindex = :defindex" +
            "  AND quality = :quality" +
            "  AND tradable = :tradable" +
            "  AND craftable = :craftable" +
            "  AND australium = :australium" +
            "  AND price_index = :priceIndex" +
            "  AND weapon_wear = :weaponWear")
    CalculatorItem find(int defindex, int quality, boolean tradable, boolean craftable,
                        boolean australium, int priceIndex, int weaponWear);

    @Query("DELETE FROM calculator")
    void deleteAll();

    @Update
    void update(CalculatorItem... calculatorItems);
}
