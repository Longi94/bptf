package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfResponse {

    @SerializedName("success")
    @Expose
    private int success;

    @SerializedName("current_time")
    @Expose
    private long currentTime;

    @SerializedName("players")
    @Expose
    private Map<String, BackpackTfPlayer> players;

    public int getSuccess() {
        return success;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public Map<String, BackpackTfPlayer> getPlayers() {
        return players;
    }
}
