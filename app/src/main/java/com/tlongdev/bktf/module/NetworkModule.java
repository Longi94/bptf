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

import com.google.gson.Gson;
import com.tlongdev.bktf.network.BackpackTfInterface;
import com.tlongdev.bktf.network.FixerIoInterface;
import com.tlongdev.bktf.network.SteamUserInterface;
import com.tlongdev.bktf.network.Tf2Interface;
import com.tlongdev.bktf.network.TlongdevInterface;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
@Module
public class NetworkModule {

    @Provides
    @Singleton
    @Named("tlongdev")
    Retrofit provideTlongdevRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(TlongdevInterface.BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }

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
    TlongdevInterface provideTlongdevInterface(@Named("tlongdev") Retrofit retrofit) {
        return retrofit.create(TlongdevInterface.class);
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
