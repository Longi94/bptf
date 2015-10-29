package com.tlongdev.bktf.test;

import android.test.AndroidTestCase;

import com.tlongdev.bktf.model.Currency;
import com.tlongdev.bktf.model.Price;

import java.lang.Exception;
import java.lang.Override;

public class PriceTest extends AndroidTestCase {

    private Price price;

    @Override
    protected void setUp() throws Exception {
        price = new Price(21.5, 23, 0, 0, 0, Currency.KEY);
    }

    public void testPrice() {
        assertEquals(21.5, price.getValue());
    }
}