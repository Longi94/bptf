package com.tlongdev.bktf.model;

public class License {

    private final String name;
    private final String url;
    private final String license;

    public License(String name, String url, String license) {
        this.name = name;
        this.url = url;
        this.license = license;
    }

    public String getName() {
        return name;
    }

    public String getLicense() {
        return license;
    }

    public String getUrl() {
        return url;
    }
}