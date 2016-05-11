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

package com.tlongdev.bktf.network.model.tlongdev;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevPricesPayload {

    @JsonProperty("success")
    private Integer success;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("prices")
    private List<TlongdevPrice> prices = new ArrayList<>();

    @JsonProperty("message")
    private String message;

    public Integer getSuccess() {
        return success;
    }

    public Integer getCount() {
        return count;
    }

    public List<TlongdevPrice> getPrices() {
        return prices;
    }

    public String getMessage() {
        return message;
    }
}
