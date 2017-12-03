package com.tlongdev.bktf.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.tlongdev.bktf.data.dao.DecoratedWeaponDao;
import com.tlongdev.bktf.data.dao.ItemSchemaDao;
import com.tlongdev.bktf.data.dao.OriginDao;
import com.tlongdev.bktf.data.dao.UnusualSchemaDao;
import com.tlongdev.bktf.data.entity.DecoratedWeapon;
import com.tlongdev.bktf.data.entity.ItemSchema;
import com.tlongdev.bktf.data.entity.Origin;
import com.tlongdev.bktf.data.entity.UnusualSchema;

/**
 * Created by lngtr on 2017-11-28.
 */
@Database(
        entities = {
                UnusualSchema.class,
                Origin.class,
                DecoratedWeapon.class,
                ItemSchema.class
        },
        version = 10
)
public abstract class BptfDatabase extends RoomDatabase {
    public abstract UnusualSchemaDao unusualSchemaDao();

    public abstract OriginDao originDao();

    public abstract DecoratedWeaponDao decoratedWeaponDao();

    public abstract ItemSchemaDao itemSchemaDao();
}
