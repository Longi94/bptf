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
import android.arch.persistence.room.Room;
import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;

import com.tlongdev.bktf.data.BptfDatabase;
import com.tlongdev.bktf.data.DatabaseHelper;
import com.tlongdev.bktf.data.dao.DecoratedWeaponDao;
import com.tlongdev.bktf.data.dao.ItemSchemaDao;
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
                        BptfDatabase.MIGRATION_9_10
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

    @Provides
    @Singleton
    ItemSchemaDao provideItemSchemaDao(BptfDatabase database) {
        return database.itemSchemaDao();
    }
}
