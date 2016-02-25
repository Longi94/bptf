package com.tlongdev.bktf.api;

import com.tlongdev.bktf.model.CurrencyRates;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Long
 * @since 2016. 02. 25.
 */
public interface FixerIoInterface {

    String BASE_URL = "http://api.fixer.io/";

    @GET("latest")
    Call<CurrencyRates> getCurrencyRates(@Query("base") String base);
}
