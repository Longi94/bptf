package com.tlongdev.bktf.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevItem {

    @SerializedName("defindex")
    @Expose
    private Integer defindex;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("description")
    @Expose
    private Object description;

    @SerializedName("type_name")
    @Expose
    private String typeName;

    @SerializedName("proper_name")
    @Expose
    private Integer properName;

    public Integer getDefindex() {
        return defindex;
    }

    public String getName() {
        return name;
    }

    public Object getDescription() {
        return description;
    }

    public String getTypeName() {
        return typeName;
    }

    public Integer getProperName() {
        return properName;
    }
}
