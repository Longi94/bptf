package com.tlongdev.bktf.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevPricesPayload {

    @SerializedName("success")
    @Expose
    private Integer success;

    @SerializedName("count")
    @Expose
    private Integer count;

    @SerializedName("prices")
    @Expose
    private List<TlongdevPrice> prices = new ArrayList<TlongdevPrice>();

    @SerializedName("message")
    @Expose
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
