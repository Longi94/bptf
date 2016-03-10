/**
 * Copyright 2015 Long Tran
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

package com.tlongdev.bktf.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevPrice {

    @SerializedName("defindex")
    @Expose
    private Integer defindex;

    @SerializedName("item_name")
    @Expose
    private String itemName;

    @SerializedName("quality")
    @Expose
    private Integer quality;

    @SerializedName("tradable")
    @Expose
    private Integer tradable;

    @SerializedName("craftable")
    @Expose
    private Integer craftable;

    @SerializedName("price_index")
    @Expose
    private Integer priceIndex;

    @SerializedName("australium")
    @Expose
    private Integer australium;

    @SerializedName("value")
    @Expose
    private Double value;

    @SerializedName("value_raw")
    @Expose
    private Double valueRaw;

    @SerializedName("currency")
    @Expose
    private String currency;

    @SerializedName("last_update")
    @Expose
    private Long lastUpdate;

    @SerializedName("difference")
    @Expose
    private Double difference;

    @SerializedName("weapon_wear")
    @Expose
    private Integer weaponWear;

    @SerializedName("value_high")
    @Expose
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
