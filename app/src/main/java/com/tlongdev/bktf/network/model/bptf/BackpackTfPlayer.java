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
    public String steamid;

    @SerializedName("success")
    @Expose
    public int success;

    @SerializedName("backpack_value")
    @Expose
    public BackpackTfBackpackValue backpackValue;

    @SerializedName("backpack_update")
    @Expose
    public BackpackTfBackpackUpdate backpackUpdate;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("backpack_tf_reputation")
    @Expose
    public int backpackTfReputation;

    @SerializedName("backpack_tf_group")
    @Expose
    public boolean backpackTfGroup;

    @SerializedName("backpack_tf_banned")
    @Expose
    public boolean backpackTfBanned;

    @SerializedName("backpack_tf_bans")
    @Expose
    public BackpackTfBans backpackTfBans;

    @SerializedName("backpack_tf_trust")
    @Expose
    public BackpackTfTrust backpackTfTrust;

    @SerializedName("steamrep_scammer")
    @Expose
    public boolean steamrepScammer;

    @SerializedName("ban_economy")
    @Expose
    public boolean banEconomy;

    @SerializedName("ban_community")
    @Expose
    public boolean banCommunity;

    @SerializedName("ban_vac")
    @Expose
    public boolean banVac;

    @SerializedName("notifications")
    @Expose
    public int notifications;

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
