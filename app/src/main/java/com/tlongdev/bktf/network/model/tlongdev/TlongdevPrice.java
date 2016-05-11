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

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevPrice {

    @JsonProperty("defindex")
    private Integer defindex;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("quality")
    private Integer quality;

    @JsonProperty("tradable")
    private Integer tradable;

    @JsonProperty("craftable")
    private Integer craftable;

    @JsonProperty("price_index")
    private Integer priceIndex;

    @JsonProperty("australium")
    private Integer australium;

    @JsonProperty("value")
    private Double value;

    @JsonProperty("value_raw")
    private Double valueRaw;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("last_update")
    private Long lastUpdate;

    @JsonProperty("difference")
    private Double difference;

    @JsonProperty("weapon_wear")
    private Integer weaponWear;

    @JsonProperty("value_high")
    private Double valueHigh;

    public Integer getDefindex() {
        return defindex;
    }

    public String getItemName() {
        return itemName;
    }

    public Integer getQuality() {
        return quality;
    }

    public Integer getTradable() {
        return tradable;
    }

    public Integer getCraftable() {
        return craftable;
    }

    public Integer getPriceIndex() {
        return priceIndex;
    }

    public Integer getAustralium() {
        return australium;
    }

    public Double getValue() {
        return value;
    }

    public Double getValueRaw() {
        return valueRaw;
    }

    public String getCurrency() {
        return currency;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public Double getDifference() {
        return difference;
    }

    public Integer getWeaponWear() {
        return weaponWear;
    }

    public Double getValueHigh() {
        return valueHigh;
    }
}
