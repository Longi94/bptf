package com.tlongdev.bktf.network;

import com.tlongdev.bktf.network.model.fixerio.CurrencyRates;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Long
 * @since 2016. 02. 25.
 */
public interface FixerIoInterface {

    String BASE_URL = "https://api.fixer.io/";

    @GET("latest")
    Call<CurrencyRates> getCurrencyRates(@Query("base") String base);
}
