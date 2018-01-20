package com.tlongdev.bktf.network.model.steam;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class UserSummariesPlayer {

    @SerializedName("steamid")
    @Expose
    private String steamid;

    @SerializedName("communityvisibilitystate")
    @Expose
    private int communityVisibilityState;

    @SerializedName("profilestate")
    @Expose
    private int profileState;

    @SerializedName("personaname")
    @Expose
    private String personaName;

    @SerializedName("lastlogoff")
    @Expose
    private long lastLogoff;

    @SerializedName("profileurl")
    @Expose
    private String profileUrl;

    @SerializedName("avatar")
    @Expose
    private String avatar;

    @SerializedName("avatarmedium")
    @Expose
    private String avatarMedium;

    @SerializedName("avatarfull")
    @Expose
    private String avatarFull;

    @SerializedName("personastate")
    @Expose
    private int personaState;

    @SerializedName("realname")
    @Expose
    private String realName;

    @SerializedName("primaryclanid")
    @Expose
    private String primaryClanId;

    @SerializedName("timecreated")
    @Expose
    private int timeCreated;

    @SerializedName("personastateflags")
    @Expose
    private int personaStateFlags;

    @SerializedName("gameid")
    @Expose
    private int gameId;

    public String getSteamid() {
        return steamid;
    }

    public int getCommunityVisibilityState() {
        return communityVisibilityState;
    }

    public int getProfileState() {
        return profileState;
    }

    public String getPersonaName() {
        return personaName;
    }

    public long getLastLogoff() {
        return lastLogoff;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAvatarMedium() {
        return avatarMedium;
    }

    public String getAvatarFull() {
        return avatarFull;
    }

    public int getPersonaState() {
        return personaState;
    }

    public String getRealName() {
        return realName;
    }

    public String getPrimaryClanId() {
        return primaryClanId;
    }

    public int getTimeCreated() {
        return timeCreated;
    }

    public int getPersonaStateFlags() {
        return personaStateFlags;
    }

    public int getGameId() {
        return gameId;
    }
}
