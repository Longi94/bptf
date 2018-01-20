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
    private long id;

    @SerializedName("original_id")
    @Expose
    private long originalId;

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
    private long inventory;

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

    public long getId() {
        return id;
    }

    public long getOriginalId() {
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

    public long getInventory() {
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
