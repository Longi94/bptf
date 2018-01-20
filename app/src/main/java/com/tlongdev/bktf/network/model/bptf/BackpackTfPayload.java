package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfPayload {

    @SerializedName("response")
    @Expose
    private BackpackTfResponse response;

    public BackpackTfResponse getResponse() {
        return response;
    }
}
