package com.tlongdev.bktf.model;

import java.util.Map;

/**
 * @author lngtr
 * @since 2016. 05. 09.
 */
public class CurrencyRates {
    private Map<String, Double> rates;

    private long lastUpdate;

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
