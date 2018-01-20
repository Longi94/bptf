package com.tlongdev.bktf.network.model.steam;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class UserSummariesResponse {

    @SerializedName("players")
    @Expose
    private List<UserSummariesPlayer> players = new ArrayList<>();

    public List<UserSummariesPlayer> getPlayers() {
        return players;
    }
}
