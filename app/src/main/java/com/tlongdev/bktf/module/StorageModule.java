package com.tlongdev.bktf.module;

import android.app.Application;
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
        return new DatabaseHelper(application).getReadableDatabase();
    }

    @Provides
    @Singleton
    @Named("writable")
    SQLiteDatabase provideWritableDatabase(Application application) {
        return new DatabaseHelper(application).getWritableDatabase();
    }
}
