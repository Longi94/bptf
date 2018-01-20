package com.tlongdev.bktf.model;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Currency types.
 */
@SuppressWarnings("unused")
public class Currency {

    @StringDef({USD, METAL, KEY, BUD, HAT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Enum {

    }
    public static final String USD = "usd";
    public static final String METAL = "metal";
    public static final String KEY = "keys";
    public static final String BUD = "earbuds";
    public static final String HAT = "hat";
}
