package com.tlongdev.bktf.util;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Long
 * @since 2016. 02. 25.
 */
public class TestUtils {

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
