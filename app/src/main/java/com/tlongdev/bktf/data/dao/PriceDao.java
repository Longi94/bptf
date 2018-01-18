package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import com.tlongdev.bktf.data.entity.Price;

import java.util.Collection;

@Dao
public interface PriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Collection<Price> prices);

    @Query("SELECT * FROM pricelist ORDER BY last_update DESC LIMIT 1")
    Price getNewestPrice();

    @Query("SELECT * FROM pricelist " +
            "WHERE defindex = :defindex" +
            "  AND quality = :quality" +
            "  AND tradable = :tradable" +
            "  AND craftable = :craftable" +
            "  AND australium = :australium" +
            "  AND price_index = :priceIndex" +
            "  AND weapon_wear = :weaponWear")
    Price find(int defindex, int quality, boolean tradable, boolean craftable,
               boolean australium, int priceIndex, int weaponWear);

    @Query("SELECT * FROM pricelist " +
            "WHERE defindex IN (143, 5002, 5021)" +
            "  AND quality = 6" +
            "  AND tradable = 1" +
            "  AND craftable = 1")
    Price[] getCurrencyPrices();

    @Query("SELECT " +
            "pricelist._id, " +
            "pricelist.defindex, " +
            "item_schema.item_name, " +
            "pricelist.quality, " +
            "pricelist.tradable, " +
            "pricelist.craftable, " +
            "pricelist.price_index, " +
            "pricelist.currency, " +
            "pricelist.price, " +
            "pricelist.max, " +
            "pricelist.difference, " +
            "pricelist.australium " +
            "FROM pricelist " +
            "LEFT JOIN item_schema " +
            "       ON pricelist.defindex = item_schema.defindex " +
            "ORDER BY last_update DESC")
    Cursor findAll();
}
