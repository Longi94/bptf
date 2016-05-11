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

package com.tlongdev.bktf.network.model.tlongdev;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevItem {

    @JsonProperty("defindex")
    private Integer defindex;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type_name")
    private String typeName;

    @JsonProperty("proper_name")
    private Integer properName;

    public Integer getDefindex() {
        return defindex;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTypeName() {
        return typeName;
    }

    public Integer getProperName() {
        return properName;
    }
}
