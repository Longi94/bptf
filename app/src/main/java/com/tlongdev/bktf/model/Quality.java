package com.tlongdev.bktf.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Quality types.
 */
public class Quality {

    @IntDef({NORMAL, GENUINE, UNUSED, VINTAGE, UNUSED2, UNUSUAL, UNIQUE, COMMUNITY, VALVE, SELF_MADE
            , UNUSED3, STRANGE, UNUSED4, HAUNTED, COLLECTORS, PAINTKITWEAPON})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Enum {
    }

    public static final int NORMAL = 0;
    public static final int GENUINE = 1;
    public static final int UNUSED = 2;
    public static final int VINTAGE = 3;
    public static final int UNUSED2 = 4;
    public static final int UNUSUAL = 5;
    public static final int UNIQUE = 6;
    public static final int COMMUNITY = 7;
    public static final int VALVE = 8;
    public static final int SELF_MADE = 9;
    public static final int UNUSED3 = 10;
    public static final int STRANGE = 11;
    public static final int UNUSED4 = 12;
    public static final int HAUNTED = 13;
    public static final int COLLECTORS = 14;
    public static final int PAINTKITWEAPON = 15;
}
