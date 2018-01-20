package com.tlongdev.bktf.network.model.tf2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class PlayerItemAccountInfo {

    @SerializedName("steamid")
    @Expose
    private String steamid;

    @SerializedName("personaname")
    @Expose
    private String personaName;

    public String getSteamid() {
        return steamid;
    }

    public String getPersonaName() {
        return personaName;
    }
}
