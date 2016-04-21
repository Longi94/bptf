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

package com.tlongdev.bktf.adapter.spinner;

import android.content.Context;
import android.widget.ArrayAdapter;

public class WeaponWearAdapter extends ArrayAdapter<String> {

    private static final String[] WEAPON_WEARS = {
            "Factory New", "Minimal Wear", "Field-Tested", "Well Worn", "Battle Scarred"
    };

    private static final int[] WEAPON_WEAR_IDS = {
            1045220557, 1053609165, 1058642330, 1061997773, 1065353216
    };

    public WeaponWearAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, WEAPON_WEARS);
    }

    public int getWearId(int selectedItemPosition) {
        return WEAPON_WEAR_IDS[selectedItemPosition];
    }
}
