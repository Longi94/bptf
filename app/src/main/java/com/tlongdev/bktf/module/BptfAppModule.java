package com.tlongdev.bktf.module;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.tlongdev.bktf.BptfApplication;
import com.tlongdev.bktf.ui.NavigationDrawerManager;
import com.tlongdev.bktf.util.CurrencyRatesManager;
import com.tlongdev.bktf.util.ProfileManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
@Module
public class BptfAppModule {

    private final Application mApplication;

    public BptfAppModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    SharedPreferences.Editor provideEditor(SharedPreferences prefs) {
        return prefs.edit();
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    ProfileManager provideProfileManager(Application application) {
        return ProfileManager.getInstance(application);
    }

    @Provides
    @Singleton
    NavigationDrawerManager provideNavigationDrawerManager(Application application) {
        return new NavigationDrawerManager(application);
    }

    @Provides
    @Singleton
    CurrencyRatesManager provideCurrencyRatesManager(BptfApplication application) {
        return CurrencyRatesManager.getInstance(application);
    }
}
