package com.tlongdev.bktf.network.model.tf2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class PlayerItemEquipped {

    @SerializedName("class")
    @Expose
    private int _class;

    @SerializedName("slot")
    @Expose
    private int slot;

    public int get_class() {
        return _class;
    }

    public int getSlot() {
        return slot;
    }
}
