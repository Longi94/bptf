package com.tlongdev.bktf.network;

import com.tlongdev.bktf.network.model.bptf.BackpackTfPayload;
import com.tlongdev.bktf.network.model.bptf.BackpackTfPriceHistoryPayload;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public interface BackpackTfInterface {

    String BASE_URL = "https://backpack.tf/api/";

    @GET("IGetUsers/v3/")
    Call<BackpackTfPayload> getUserData(@Query("steamids") String steamId);

    @GET("IGetPriceHistory/v1/")
    Call<BackpackTfPriceHistoryPayload> getPriceHistory(@Query("key") String apiKey,
                                                        @Query("item") String item,
                                                        @Query("quality") int quality,
                                                        @Query("tradable") int tradable,
                                                        @Query("craftable") int craftable,
                                                        @Query("priceindex") int priceIndex);
}
