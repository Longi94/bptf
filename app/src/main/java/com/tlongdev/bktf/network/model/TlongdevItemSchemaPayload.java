package com.tlongdev.bktf.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevItemSchemaPayload {

    @SerializedName("success")
    @Expose
    private Integer success;

    @SerializedName("items")
    @Expose
    private List<TlongdevItem> items = new ArrayList<TlongdevItem>();

    @SerializedName("message")
    @Expose
    private String message;

    public Integer getSuccess() {
        return success;
    }

    public List<TlongdevItem> getItems() {
        return items;
    }

    public String getMessage() {
        return message;
    }
}
