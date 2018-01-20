package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfTrust {

    @SerializedName("for")
    @Expose
    private int _for;

    @SerializedName("against")
    @Expose
    private int against;

    public int getFor() {
        return _for;
    }

    public int getAgainst() {
        return against;
    }
}
