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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevItemSchemaPayload {

    @JsonProperty("success")
    private Integer success;

    @JsonProperty("items")
    private List<TlongdevItem> items = new ArrayList<>();

    @JsonProperty("origins")
    private List<TlongdevOrigin> origins = new ArrayList<>();

    @JsonProperty("particle_names")
    private List<TlongdevParticleName> particleName = new ArrayList<>();

    @JsonProperty("decorated_weapons")
    private List<TlongdevDecoratedWeapon> decoratedWeapons = new ArrayList<>();

    @JsonProperty("message")
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
