package com.tlongdev.bktf.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevDecoratedWeapon {

    @SerializedName("defindex")
    @Expose
    private int defindex;

    @SerializedName("grade")
    @Expose
    private int grade;

    public int getDefindex() {
        return defindex;
    }

    public int getGrade() {
        return grade;
    }
}
