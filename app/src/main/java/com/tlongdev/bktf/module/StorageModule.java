/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlongdev.bktf.module;

import android.app.Application;
import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;

import com.tlongdev.bktf.data.DatabaseHelper;

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
}
