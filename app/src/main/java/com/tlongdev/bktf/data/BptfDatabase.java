package com.tlongdev.bktf.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

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

    public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // migrating to room

            // migrate unusual schema
            database.execSQL("CREATE TABLE unusual_schema_copy (_id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL)");
            database.execSQL("INSERT INTO unusual_schema_copy (_id, name) SELECT id, name FROM unusual_schema");
            database.execSQL("DROP TABLE unusual_schema");
            database.execSQL("ALTER TABLE unusual_schema_copy RENAME TO unusual_schema");

            // migrate origins
            database.execSQL("CREATE TABLE origin_names_copy (_id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL)");
            database.execSQL("INSERT INTO origin_names_copy (_id, name) SELECT id, name FROM origin_names");
            database.execSQL("DROP TABLE origin_names");
            database.execSQL("ALTER TABLE origin_names_copy RENAME TO origin_names");

            // migrate decorated weapons
            database.execSQL("CREATE TABLE decorated_weapons_copy (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, defindex INTEGER NOT NULL, grade INTEGER NOT NULL)");
            database.execSQL("INSERT INTO decorated_weapons_copy (_id, defindex, grade) SELECT _id, defindex, grade FROM decorated_weapons");
            database.execSQL("DROP TABLE decorated_weapons");
            database.execSQL("ALTER TABLE decorated_weapons_copy RENAME TO decorated_weapons");

            // migrate item schema
            database.execSQL("CREATE TABLE item_schema_copy (_id INTEGER NOT NULL, defindex INTEGER NOT NULL, item_name TEXT NOT NULL, description TEXT, type_name TEXT NOT NULL, proper_name INTEGER NOT NULL, PRIMARY KEY(_id))");
            database.execSQL("INSERT INTO item_schema_copy (_id, defindex, item_name, description, type_name, proper_name) SELECT _id, defindex, item_name, description, type_name, proper_name FROM item_schema");
            database.execSQL("DROP TABLE item_schema");
            database.execSQL("ALTER TABLE item_schema_copy RENAME TO item_schema");
        }
    };

    public abstract UnusualSchemaDao unusualSchemaDao();

    public abstract OriginDao originDao();

    public abstract DecoratedWeaponDao decoratedWeaponDao();

    public abstract ItemSchemaDao itemSchemaDao();
}
