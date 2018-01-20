package com.tlongdev.bktf.network;

import com.tlongdev.bktf.network.model.tf2.PlayerItemsPayload;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public interface Tf2Interface {
    String BASE_URL = "http://api.steampowered.com/IEconItems_440/";

    @GET("GetPlayerItems/v0001/")
    Call<PlayerItemsPayload> getUserBackpack(@Query("key") String apiKey, @Query("SteamID") String steamId);
}
