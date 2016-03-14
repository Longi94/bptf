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

import java.util.Map;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class BackpackTfResponse {

    @SerializedName("success")
    @Expose
    private int success;

    @SerializedName("current_time")
    @Expose
    private long currentTime;

    @SerializedName("players")
    @Expose
    private Map<String, BackpackTfPlayer> players;

    public int getSuccess() {
        return success;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public Map<String, BackpackTfPlayer> getPlayers() {
        return players;
    }
}