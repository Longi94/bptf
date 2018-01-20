package com.tlongdev.bktf.network.model.steam;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class UserSummariesPayload {

    @SerializedName("response")
    @Expose
    private UserSummariesResponse response;

    public UserSummariesResponse getResponse() {
        return response;
    }
}
