package com.tlongdev.bktf.network.model.steam;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class VanityUrlResponse {

    @SerializedName("steamid")
    @Expose
    private String steamid;

    @SerializedName("success")
    @Expose
    private int success;

    @SerializedName("message")
    @Expose
    private String message;

    public String getSteamid() {
        return steamid;
    }

    public int getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
