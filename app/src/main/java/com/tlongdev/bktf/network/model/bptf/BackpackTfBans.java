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
    private boolean all;

    @SerializedName("suggestions")
    @Expose
    private boolean suggestions;

    @SerializedName("comments")
    @Expose
    private boolean comments;

    @SerializedName("trust")
    @Expose
    private boolean trust;

    @SerializedName("issues")
    @Expose
    private boolean issues;

    @SerializedName("chat")
    @Expose
    private boolean chat;

    @SerializedName("classifieds")
    @Expose
    private boolean classifieds;

    @SerializedName("lotto")
    @Expose
    private boolean lotto;

    @SerializedName("customizations")
    @Expose
    private boolean customizations;

    @SerializedName("reports")
    @Expose
    private boolean reports;

    public boolean getAll() {
        return all;
    }

    public boolean getSuggestions() {
        return suggestions;
    }

    public boolean getComments() {
        return comments;
    }

    public boolean getTrust() {
        return trust;
    }

    public boolean getIssues() {
        return issues;
    }

    public boolean getChat() {
        return chat;
    }

    public boolean getClassifieds() {
        return classifieds;
    }

    public boolean getLotto() {
        return lotto;
    }

    public boolean getCustomizations() {
        return customizations;
    }

    public boolean getReports() {
        return reports;
    }
}
