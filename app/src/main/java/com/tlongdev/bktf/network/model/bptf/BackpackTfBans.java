/**
 * Copyright 2016 Long Tran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
