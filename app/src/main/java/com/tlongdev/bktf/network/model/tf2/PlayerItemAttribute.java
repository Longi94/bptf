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

package com.tlongdev.bktf.network.model.tf2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Long
 * @since 2016. 03. 14.
 */
public class PlayerItemAttribute {

    @SerializedName("defindex")
    @Expose
    private int defindex;

    @SerializedName("value")
    @Expose
    private int value;

    @SerializedName("float_value")
    @Expose
    private float floatValue;

    @SerializedName("account_info")
    @Expose
    private PlayerItemAccountInfo accountInfo;

    public int getDefindex() {
        return defindex;
    }

    public int getValue() {
        return value;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public PlayerItemAccountInfo getAccountInfo() {
        return accountInfo;
    }
}
