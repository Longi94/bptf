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
