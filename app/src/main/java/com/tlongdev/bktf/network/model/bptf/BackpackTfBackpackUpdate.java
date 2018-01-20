package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfBackpackUpdate {

    @SerializedName("440")
    @Expose
    private long _440;

    @SerializedName("570")
    @Expose
    private long _570;

    public long get440() {
        return _440;
    }

    public long get570() {
        return _570;
    }
}
