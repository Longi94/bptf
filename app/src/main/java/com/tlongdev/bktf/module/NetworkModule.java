package com.tlongdev.bktf.module;

import com.google.gson.Gson;
import com.tlongdev.bktf.network.TlongdevInterface;

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
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    @Named("tlongdev")
    Retrofit provideRetrofit(Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(TlongdevInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    TlongdevInterface provideTlongdevInterface(@Named("tlongdev") Retrofit retrofit) {
        return retrofit.create(TlongdevInterface.class);
    }
}
