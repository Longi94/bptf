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

package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfPlayer {

    @SerializedName("steamid")
    @Expose
    private String steamid;

    @SerializedName("success")
    @Expose
    private int success;

    @SerializedName("backpack_value")
    @Expose
    private BackpackTfBackpackValue backpackValue;

    @SerializedName("backpack_update")
    @Expose
    private BackpackTfBackpackUpdate backpackUpdate;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("backpack_tf_reputation")
    @Expose
    private int backpackTfReputation;

    @SerializedName("backpack_tf_group")
    @Expose
    private boolean backpackTfGroup;

    @SerializedName("backpack_tf_banned")
    @Expose
    private boolean backpackTfBanned;

    @SerializedName("backpack_tf_bans")
    @Expose
    private BackpackTfBans backpackTfBans;

    @SerializedName("backpack_tf_trust")
    @Expose
    private BackpackTfTrust backpackTfTrust;

    @SerializedName("steamrep_scammer")
    @Expose
    private boolean steamrepScammer;

    @SerializedName("ban_economy")
    @Expose
    private boolean banEconomy;

    @SerializedName("ban_community")
    @Expose
    private boolean banCommunity;

    @SerializedName("ban_vac")
    @Expose
    private boolean banVac;

    @SerializedName("notifications")
    @Expose
    private int notifications;

    public String getSteamid() {
        return steamid;
    }

    public int getSuccess() {
        return success;
    }

    public BackpackTfBackpackValue getBackpackValue() {
        return backpackValue;
    }

    public BackpackTfBackpackUpdate getBackpackUpdate() {
        return backpackUpdate;
    }

    public String getName() {
        return name;
    }

    public int getBackpackTfReputation() {
        return backpackTfReputation;
    }

    public boolean getBackpackTfGroup() {
        return backpackTfGroup;
    }

    public boolean getBackpackTfBanned() {
        return backpackTfBanned;
    }

    public BackpackTfBans getBackpackTfBans() {
        return backpackTfBans;
    }

    public BackpackTfTrust getBackpackTfTrust() {
        return backpackTfTrust;
    }

    public boolean getSteamrepScammer() {
        return steamrepScammer;
    }

    public boolean getBanEconomy() {
        return banEconomy;
    }

    public boolean getBanCommunity() {
        return banCommunity;
    }

    public boolean getBanVac() {
        return banVac;
    }

    public int getNotifications() {
        return notifications;
    }
}
