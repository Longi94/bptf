package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfPriceHistoryResponse {

    @SerializedName("success")
    @Expose
    private int success;

    @SerializedName("history")
    @Expose
    private final List<BackpackTfPriceHistory> history = new ArrayList<>();

    @SerializedName("message")
    @Expose
    private String message;

    public int getSuccess() {
        return success;
    }

    public List<BackpackTfPriceHistory> getHistory() {
        return history;
    }

    public String getMessage() {
        return message;
    }
}
