package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfPriceHistoryPayload {

    @SerializedName("response")
    @Expose
    private BackpackTfPriceHistoryResponse response;

    public BackpackTfPriceHistoryResponse getResponse() {
        return response;
    }
}
