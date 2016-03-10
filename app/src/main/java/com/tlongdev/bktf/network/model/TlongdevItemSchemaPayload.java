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
    private List<TlongdevItem> items = new ArrayList<>();

    @SerializedName("origins")
    @Expose
    private List<TlongdevOrigin> origins = new ArrayList<>();

    @SerializedName("particle_names")
    @Expose
    private List<TlongdevParticleName> particleName = new ArrayList<>();

    @SerializedName("decorated_weapons")
    @Expose
    private List<TlongdevDecoratedWeapon> decoratedWeapons = new ArrayList<>();

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

    public List<TlongdevOrigin> getOrigins() {
        return origins;
    }

    public List<TlongdevParticleName> getParticleName() {
        return particleName;
    }

    public List<TlongdevDecoratedWeapon> getDecoratedWeapons() {
        return decoratedWeapons;
    }
}
