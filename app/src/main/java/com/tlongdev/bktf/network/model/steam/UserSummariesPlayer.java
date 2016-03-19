/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
