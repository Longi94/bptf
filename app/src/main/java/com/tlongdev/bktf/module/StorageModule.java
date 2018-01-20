package com.tlongdev.bktf.module;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;

import com.tlongdev.bktf.data.BptfDatabase;
import com.tlongdev.bktf.data.dao.BackpackDao;
import com.tlongdev.bktf.data.dao.CalculatorDao;
import com.tlongdev.bktf.data.dao.DecoratedWeaponDao;
import com.tlongdev.bktf.data.dao.FavoriteDao;
import com.tlongdev.bktf.data.dao.ItemSchemaDao;
import com.tlongdev.bktf.data.dao.OriginDao;
import com.tlongdev.bktf.data.dao.PriceDao;
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
    SupportSQLiteDatabase provideReadableDatabase(BptfDatabase database) {
        return database.getOpenHelper().getReadableDatabase();
    }

    @Provides
    @Singleton
    @Named("writable")
    SupportSQLiteDatabase provideWritableDatabase(BptfDatabase database) {
        return database.getOpenHelper().getWritableDatabase();
    }

    @Provides
    @Singleton
    BptfDatabase provideBptfDatabase(Application application) {
        return Room.databaseBuilder(application, BptfDatabase.class, BptfDatabase.DATABASE_NAME)
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

    @Provides
    @Singleton
    PriceDao providePriceDao(BptfDatabase database) {
        return database.priceDao();
    }

    @Provides
    @Singleton
    FavoriteDao provideFavoriteDao(BptfDatabase database) {
        return database.favoriteDao();
    }

    @Provides
    @Singleton
    CalculatorDao provideCalculatorDao(BptfDatabase database) {
        return database.calculatorDao();
    }

    @Provides
    @Singleton
    BackpackDao provideBackpackDao(BptfDatabase database) {
        return database.backpackDao();
    }
}
