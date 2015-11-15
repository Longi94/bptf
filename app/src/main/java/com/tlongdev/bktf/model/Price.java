package com.tlongdev.bktf.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tlongdev.bktf.R;
import com.tlongdev.bktf.util.Utility;

import java.text.DecimalFormat;

/**
 * Price class
 */
public class Price {

    /**
     * Log tag for logging.
     */
    @SuppressWarnings("unused")
    private static final String LOG_TAG = Price.class.getSimpleName();

    private double value;

    private double highValue;

    private double rawValue;

    private long lastUpdate;

    private double difference;

    private String currency;

    public Price() {
        this(0, 0, 0, 0, 0, null);
    }

    public Price(double value, String currency) {
        this(value, 0, 0, 0, 0, currency);
    }

    public Price(double value, double highValue, double rawValue, long lastUpdate, double difference, String currency) {
        this.value = value;
        this.highValue = highValue;
        this.rawValue = rawValue;
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

    public double getRawValue() {
        return rawValue;
    }

    public void setRawValue(double rawValue) {
        this.rawValue = rawValue;
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

    /**
     * Formats the given price and converts it to the desired currency.
     *
     * @param context        the context
     * @param targetCurrency the target currency
     * @return the formatted price
     */
    public String getFormattedPrice(Context context, String targetCurrency) {
        //Initial string, that will be appended.
        String product = "";

        //Convert the prices first
        double low = getConvertedPrice(context, targetCurrency, false);
        double high = 0;
        if (highValue > value)
            high = getConvertedPrice(context, targetCurrency, true);

        //Check if the price is an int
        if ((int) low == low)
            product += (int) low;
            //Check if the double has fraction smaller than 0.01, if so we need to format the double
        else if ((String.valueOf(low)).substring((String.valueOf(low)).indexOf('.') + 1).length() > 2)
            product += new DecimalFormat("#.##").format(low);
        else
            product += low;

        if (high > low) {
            //Check if the price is an int
            if ((int) high == high)
                product += "-" + (int) high;
                //Check if the double has fraction smaller than 0.01, if so we need to format the double
            else if ((String.valueOf(high)).substring((String.valueOf(high)).indexOf('.') + 1).length() > 2)
                product += "-" + new DecimalFormat("#.##").format(high);
            else
                product += "-" + high;
        }

        //Append the string with the proper currency, plural ir needed.
        switch (targetCurrency) {
            case Currency.BUD:
                if (low == 1.0 && high == 0.0)
                    return context.getString(R.string.currency_bud, product);
                else
                    return context.getString(R.string.currency_bud_plural, product);
            case Currency.METAL:
                return context.getString(R.string.currency_metal, product);
            case Currency.KEY:
                if (low == 1.0 && high == 0.0)
                    return context.getString(R.string.currency_key, product);
                else
                    return context.getString(R.string.currency_key_plural, product);
            case Currency.USD:
                return "$" + product;
            default:
                //App should never reach this code
                Log.e(LOG_TAG, "Error formatting price");
                throw new IllegalArgumentException("Error while formatting price");
        }
    }

    /**
     * Formats the given price and converts it to the desired currency.
     *
     * @param context the context
     * @return the formatted price
     */
    public String getFormattedPrice(Context context) {
        return getFormattedPrice(context, currency);
    }

    /**
     * Converts the price to the desired currency.
     *
     * @param context        the context
     * @param targetCurrency the target currency
     * @param isHigh         whether to convert the low or the high price
     * @return the converted price
     */
    public double getConvertedPrice(Context context, String targetCurrency, boolean isHigh) {

        double price = isHigh ? highValue : value;

        if (currency.equals(targetCurrency))
            //The target currency equals the original currency, nothing to do.
            return price;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //Magic converter block. ALl prices are converted according to their raw metal price.
        //Metal is the base currency
        switch (currency) {
            case Currency.BUD:
                switch (targetCurrency) {
                    case Currency.KEY:
                        return price * (Utility.getDouble(prefs, context.getString(R.string.pref_buds_raw), 1)
                                / Utility.getDouble(prefs, context.getString(R.string.pref_key_raw), 1));
                    case Currency.METAL:
                        return price * Utility.getDouble(prefs, context.getString(R.string.pref_buds_raw), 1);
                    case Currency.USD:
                        return price * (Utility.getDouble(prefs, context.getString(R.string.pref_buds_raw), 1)
                                * Utility.getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1));
                }
            case Currency.METAL:
                switch (targetCurrency) {
                    case Currency.KEY:
                        return price / Utility.getDouble(prefs, context.getString(R.string.pref_key_raw), 1);
                    case Currency.BUD:
                        return price / Utility.getDouble(prefs, context.getString(R.string.pref_buds_raw), 1);
                    case Currency.USD:
                        return price * Utility.getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1);
                }
            case Currency.KEY:
                switch (targetCurrency) {
                    case Currency.METAL:
                        return price * Utility.getDouble(prefs, context.getString(R.string.pref_key_raw), 1);
                    case Currency.BUD:
                        return price * (Utility.getDouble(prefs, context.getString(R.string.pref_key_raw), 1)
                                / Utility.getDouble(prefs, context.getString(R.string.pref_buds_raw), 1));
                    case Currency.USD:
                        return price * Utility.getDouble(prefs, context.getString(R.string.pref_key_raw), 1)
                                * Utility.getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1);
                }
            case Currency.USD:
                switch (targetCurrency) {
                    case Currency.METAL:
                        return price / Utility.getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1);
                    case Currency.BUD:
                        return price / Utility.getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1)
                                / Utility.getDouble(prefs, context.getString(R.string.pref_buds_raw), 1);
                    case Currency.KEY:
                        return price / Utility.getDouble(prefs, context.getString(R.string.pref_metal_raw_usd), 1)
                                / Utility.getDouble(prefs, context.getString(R.string.pref_key_raw), 1);
                }
            default:
                //Unknown currency was given, throw an exception.
                String error = "Unknown target currency: " + currency;
                Log.e(LOG_TAG, error);
                throw new IllegalArgumentException(error);
        }
    }

    /**
     * Check whether the price is older than 3 months.
     *
     * @return true if the price is older than 3 month
     */
    public boolean isOld() {
        return System.currentTimeMillis() - lastUpdate * 1000L > 7884000000L;
    }

    /**
     * Returns the appropriate color for the price difference
     *
     * @return the color indicating the price difference
     */
    public int getDifferenceColor() {
        if (Math.abs(difference - rawValue) < Utility.EPSILON) {
            // TODO: 2015. 10. 26. There might be inaccuracies resulting in the difference not being equal to the raw price
            return 0xFFFFFF00;
        } else if (difference == 0.0) {
            return 0xFFFFFFFF;
        } else if (difference > 0.0) {
            return 0xFF00FF00;
        } else {
            return 0xFFFF0000;
        }
    }

    /**
     * Formats the difference value.
     *
     * @param context the context
     * @return the formatted difference
     */
    public String getFormattedDifference(Context context) {
        if (Math.abs(difference - rawValue) < Utility.EPSILON) {
            // TODO: 2015. 10. 26. There might be inaccuracies resulting in the difference not being equal to the raw price
            return "new";
        } else if (difference == 0.0) {
            return "refresh";
        } else if (difference > 0.0) {
            Price differencePrice = new Price(difference, Currency.METAL);
            return String.format("+ %s", differencePrice.getFormattedPrice(context, currency));
        } else {
            Price differencePrice = new Price(-difference, Currency.METAL);
            return String.format("- %s", differencePrice.getFormattedPrice(context, currency));
        }
    }
}
