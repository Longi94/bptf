package com.tlongdev.bktf.module;

import com.google.gson.Gson;
import com.tlongdev.bktf.network.BackpackTfInterface;
import com.tlongdev.bktf.network.FixerIoInterface;
import com.tlongdev.bktf.network.SteamUserInterface;
import com.tlongdev.bktf.network.Tf2Interface;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
@Module
public class NetworkModule {

    @Provides
    @Singleton
    @Named("backpack_tf")
    Retrofit provideBackpackTfRetrofit(Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BackpackTfInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    @Named("steam_user")
    Retrofit provideSteamUserRetrofit(Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(SteamUserInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    @Named("tf2")
    Retrofit provideTf2Retrofit(Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(Tf2Interface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    @Named("fixer_io")
    Retrofit provideFixerIoRetrofit(Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(FixerIoInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    BackpackTfInterface provideBackpackTfInterface(@Named("backpack_tf") Retrofit retrofit) {
        return retrofit.create(BackpackTfInterface.class);
    }

    @Provides
    @Singleton
    SteamUserInterface provideSteamUserInterface(@Named("steam_user") Retrofit retrofit) {
        return retrofit.create(SteamUserInterface.class);
    }

    @Provides
    @Singleton
    Tf2Interface provideTf2Interface(@Named("tf2") Retrofit retrofit) {
        return retrofit.create(Tf2Interface.class);
    }

    @Provides
    @Singleton
    FixerIoInterface provideFixerIoInterface(@Named("fixer_io") Retrofit retrofit) {
        return retrofit.create(FixerIoInterface.class);
    }
}
