package com.tlongdev.bktf.network;

import com.tlongdev.bktf.network.model.steam.UserSummariesPayload;
import com.tlongdev.bktf.network.model.steam.VanityUrl;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public interface SteamUserInterface {

    String BASE_URL = "http://api.steampowered.com/ISteamUser/";

    @GET("ResolveVanityURL/v0001/")
    Call<VanityUrl> resolveVanityUrl(@Query("key") String apiKey, @Query("vanityurl") String vanityUrl);

    @GET("GetPlayerSummaries/v0002/")
    Call<UserSummariesPayload> getUserSummaries(@Query("key") String apiKey, @Query("steamids") String steamId);
}
