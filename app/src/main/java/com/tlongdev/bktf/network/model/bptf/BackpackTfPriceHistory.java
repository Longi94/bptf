package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfPriceHistory {

    @SerializedName("value")
    @Expose
    private double value;

    @SerializedName("value_high")
    @Expose
    private double valueHigh;

    @SerializedName("currency")
    @Expose
    private String currency;

    @SerializedName("timestamp")
    @Expose
    private long timestamp;

    public double getValue() {
        return value;
    }

    public double getValueHigh() {
        return valueHigh;
    }

    public String getCurrency() {
        return currency;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
