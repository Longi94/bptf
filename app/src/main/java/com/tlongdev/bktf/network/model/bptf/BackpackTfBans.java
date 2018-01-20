package com.tlongdev.bktf.network.model.bptf;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfBans {

    @SerializedName("all")
    @Expose
    private BackpackTfBanned all;

    @SerializedName("suggestions")
    @Expose
    private BackpackTfBanned suggestions;

    @SerializedName("comments")
    @Expose
    private BackpackTfBanned comments;

    @SerializedName("trust")
    @Expose
    private BackpackTfBanned trust;

    @SerializedName("issues")
    @Expose
    private BackpackTfBanned issues;

    @SerializedName("chat")
    @Expose
    private BackpackTfBanned chat;

    @SerializedName("classifieds")
    @Expose
    private BackpackTfBanned classifieds;

    @SerializedName("lotto")
    @Expose
    private BackpackTfBanned lotto;

    @SerializedName("customizations")
    @Expose
    private BackpackTfBanned customizations;

    @SerializedName("reports")
    @Expose
    private BackpackTfBanned reports;

    public BackpackTfBanned getAll() {
        return all;
    }

    public BackpackTfBanned getSuggestions() {
        return suggestions;
    }

    public BackpackTfBanned getComments() {
        return comments;
    }

    public BackpackTfBanned getTrust() {
        return trust;
    }

    public BackpackTfBanned getIssues() {
        return issues;
    }

    public BackpackTfBanned getChat() {
        return chat;
    }

    public BackpackTfBanned getClassifieds() {
        return classifieds;
    }

    public BackpackTfBanned getLotto() {
        return lotto;
    }

    public BackpackTfBanned getCustomizations() {
        return customizations;
    }

    public BackpackTfBanned getReports() {
        return reports;
    }
}
