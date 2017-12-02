/**
 * Copyright 2016 Long Tran
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.module;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.tlongdev.bktf.data.BptfDatabase;
import com.tlongdev.bktf.data.DatabaseHelper;
import com.tlongdev.bktf.data.dao.DecoratedWeaponDao;
import com.tlongdev.bktf.data.dao.OriginDao;
import com.tlongdev.bktf.data.dao.UnusualSchemaDao;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
@Module
public class StorageModule {

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
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
            database.execSQL("CREATE TABLE IF NOT EXISTS decorated_weapons_copy (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, defindex INTEGER NOT NULL, grade INTEGER NOT NULL)");
            database.execSQL("INSERT INTO decorated_weapons_copy (_id, defindex, grade) SELECT _id, defindex, grade FROM decorated_weapons");
            database.execSQL("DROP TABLE decorated_weapons");
            database.execSQL("ALTER TABLE decorated_weapons_copy RENAME TO decorated_weapons");
        }
    };

    @Provides
    @Singleton
    @Named("readable")
    SQLiteDatabase provideReadableDatabase(Application application) {
        return DatabaseHelper.getInstance(application).getReadableDatabase();
    }

    @Provides
    @Singleton
    @Named("writable")
    SQLiteDatabase provideWritableDatabase(Application application) {
        return DatabaseHelper.getInstance(application).getWritableDatabase();
    }

    @Provides
    @Singleton
    ContentResolver provideContentResolver(Application application) {
        return application.getContentResolver();
    }

    @Provides
    @Singleton
    BptfDatabase provideBptfDatabase(Application application) {
        return Room.databaseBuilder(application, BptfDatabase.class, "bptf.db")
                .addMigrations(
                        MIGRATION_9_10
                )
                .allowMainThreadQueries()
                .build();
    }

    @Provides
    @Singleton
    UnusualSchemaDao provideUnusualSchemaDao(BptfDatabase database) {
        return database.unusualSchemaDao();
    }

    @Provides
    @Singleton
    OriginDao provideOriginDao(BptfDatabase database) {
        return database.originDao();
    }

    @Provides
    @Singleton
    DecoratedWeaponDao provideDecoratedWeaponDao(BptfDatabase database) {
        return database.decoratedWeaponDao();
    }
}
