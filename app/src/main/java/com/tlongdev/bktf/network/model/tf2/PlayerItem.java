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

package com.tlongdev.bktf.network.model.tf2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class PlayerItem {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("original_id")
    @Expose
    private int originalId;

    @SerializedName("defindex")
    @Expose
    private int defindex;

    @SerializedName("level")
    @Expose
    private int level;

    @SerializedName("quantity")
    @Expose
    private int quantity;

    @SerializedName("origin")
    @Expose
    private int origin = -1;

    @SerializedName("flag_cannot_trade")
    @Expose
    private boolean flagCannotTrade;

    @SerializedName("flag_cannot_craft")
    @Expose
    private boolean flagCannotCraft;

    @SerializedName("inventory")
    @Expose
    private int inventory;

    @SerializedName("quality")
    @Expose
    private int quality;

    @SerializedName("custom_name")
    @Expose
    private String customName;

    @SerializedName("custom_desc")
    @Expose
    private String customDesc;

    // TODO: 2016. 03. 14. containedItem

    @SerializedName("style")
    @Expose
    private int style;

    @SerializedName("attributes")
    @Expose
    private List<PlayerItemAttribute> attributes = new ArrayList<>();

    @SerializedName("equipped")
    @Expose
    private List<PlayerItemEquipped> equipped = new ArrayList<>();

    public int getId() {
        return id;
    }

    public int getOriginalId() {
        return originalId;
    }

    public int getDefindex() {
        return defindex;
    }

    public int getLevel() {
        return level;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getOrigin() {
        return origin;
    }

    public boolean isFlagCannotTrade() {
        return flagCannotTrade;
    }

    public boolean isFlagCannotCraft() {
        return flagCannotCraft;
    }

    public int getInventory() {
        return inventory;
    }

    public int getQuality() {
        return quality;
    }

    public String getCustomName() {
        return customName;
    }

    public String getCustomDesc() {
        return customDesc;
    }

    public int getStyle() {
        return style;
    }

    public List<PlayerItemAttribute> getAttributes() {
        return attributes;
    }

    public List<PlayerItemEquipped> getEquipped() {
        return equipped;
    }
}
