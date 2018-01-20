package com.tlongdev.bktf.network.model.tf2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class PlayerItemAttribute {

    @SerializedName("defindex")
    @Expose
    private int defindex;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("float_value")
    @Expose
    private float floatValue;

    @SerializedName("account_info")
    @Expose
    private PlayerItemAccountInfo accountInfo;

    public int getDefindex() {
        return defindex;
    }

    public String getValue() {
        return value;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public PlayerItemAccountInfo getAccountInfo() {
        return accountInfo;
    }
}
