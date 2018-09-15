package com.tlongdev.bktf.util;

import retrofit2.Response;

public class HttpUtil {
    public static String buildErrorMessage(Response response) {
        return String.valueOf(response.code()) +
                ' ' +
                response.message();
    }

    public static String buildErrorMessage(okhttp3.Response response) {
        return String.valueOf(response.code()) +
                ' ' +
                response.message();
    }
}
