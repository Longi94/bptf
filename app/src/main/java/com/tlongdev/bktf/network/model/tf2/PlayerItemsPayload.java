package com.tlongdev.bktf.network.model.tf2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class PlayerItemsPayload {

    @SerializedName("result")
    @Expose
    private PlayerItemsResult result;

    public PlayerItemsResult getResult() {
        return result;
    }
}
