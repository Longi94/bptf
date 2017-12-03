package com.tlongdev.bktf.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import com.tlongdev.bktf.data.entity.ItemSchema;

import java.util.Collection;

/**
 * Created by lngtr on 2017-12-02.
 */
@Dao
public interface ItemSchemaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSchemas(Collection<ItemSchema> schemas);

    @Query("SELECT * FROM item_schema WHERE item_name LIKE '%' || :itemName || '%' ORDER BY item_name ASC")
    Cursor findItemSchemasCursor(String itemName);

    @Query("SELECT * FROM item_schema WHERE defindex = :defindex")
    ItemSchema getItemSchema(int defindex);
}
