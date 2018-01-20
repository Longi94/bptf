package com.tlongdev.bktf.network.model.tf2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class PlayerItemsResult {

    @SerializedName("status")
    @Expose
    private int status;

    @SerializedName("num_backpack_slots")
    @Expose
    private int numBackpackSlots;

    @SerializedName("items")
    @Expose
    private List<PlayerItem> items;

    public int getStatus() {
        return status;
    }

    public int getNumBackpackSlots() {
        return numBackpackSlots;
    }

    public List<PlayerItem> getItems() {
        return items;
    }
}
