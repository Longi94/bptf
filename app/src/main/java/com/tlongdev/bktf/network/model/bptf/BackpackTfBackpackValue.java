package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfBackpackValue {

    @SerializedName("440")
    @Expose
    private double _440;

    @SerializedName("570")
    @Expose
    private double _570;

    public double get440() {
        return _440;
    }

    public double get570() {
        return _570;
    }
}
