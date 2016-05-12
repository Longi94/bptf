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

package com.tlongdev.bktf.network;

import com.tlongdev.bktf.network.model.tlongdev.TlongdevItemSchemaPayload;

import okhttp3.ResponseBody;
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
    Call<ResponseBody> getPrices(@Query("since") long since);

    @GET("item_schema")
    Call<TlongdevItemSchemaPayload> getItemSchema();
}
