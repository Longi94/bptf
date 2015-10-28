package com.tlongdev.bktf.model;

/**
 * Created by Long on 2015. 10. 28..
 */
public class Price {

    private double value;

    private double highValue;

    private long lastUpdate;

    private double difference;

    private String currency;

    public Price() {
        this(0, 0, 0, 0, null);
    }

    public Price(double value, double highValue, long lastUpdate, double difference, String currency) {
        this.value = value;
        this.highValue = highValue;
        this.lastUpdate = lastUpdate;
        this.difference = difference;
        this.currency = currency;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getHighValue() {
        return highValue;
    }

    public void setHighValue(double highValue) {
        this.highValue = highValue;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public double getDifference() {
        return difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
