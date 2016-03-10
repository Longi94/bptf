package com.tlongdev.bktf.network;

import com.tlongdev.bktf.network.model.TlongdevItemSchemaPayload;
import com.tlongdev.bktf.network.model.TlongdevPricesPayload;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public interface TlongdevInterface {

    String BASE_URL = "http://tlongdev.com/api/v1/";

    @GET("prices")
    Call<TlongdevPricesPayload> getPrices(@Query("since") long since);

    @GET("item_schema")
    Call<TlongdevItemSchemaPayload> getItemSchema();
}
