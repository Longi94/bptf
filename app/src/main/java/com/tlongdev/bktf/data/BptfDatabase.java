package com.tlongdev.bktf.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.tlongdev.bktf.data.dao.OriginDao;
import com.tlongdev.bktf.data.dao.UnusualSchemaDao;
import com.tlongdev.bktf.data.entity.Origin;
import com.tlongdev.bktf.data.entity.UnusualSchema;

/**
 * Created by lngtr on 2017-11-28.
 */
@Database(entities = {UnusualSchema.class, Origin.class}, version = 10, exportSchema = false)
public abstract class BptfDatabase extends RoomDatabase {
    public abstract UnusualSchemaDao unusualSchemaDao();
    public abstract OriginDao originDao();
}
