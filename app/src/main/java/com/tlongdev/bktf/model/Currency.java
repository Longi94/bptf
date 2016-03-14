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

package com.tlongdev.bktf.model;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Currency types.
 */
@SuppressWarnings("unused")
public class Currency {

    @StringDef({USD, METAL, KEY, BUD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Enum {
    }

    public static final String USD = "usd";
    public static final String METAL = "metal";
    public static final String KEY = "keys";
    public static final String BUD = "earbuds";
}
