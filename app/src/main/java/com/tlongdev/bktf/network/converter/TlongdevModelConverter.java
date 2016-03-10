package com.tlongdev.bktf.network.converter;

import com.tlongdev.bktf.model.Price;
import com.tlongdev.bktf.network.model.TlongdevPrice;

/**
 * @author Long
 * @since 2016. 03. 10.
 */
public class TlongdevModelConverter {
    public static Price convertToPrice(TlongdevPrice price) {
        return new Price(price.getValue(), price.getValueHigh() == null ? 0 : price.getValueHigh(),
                price.getValueRaw(), price.getLastUpdate(), price.getDifference(), price.getCurrency());
    }
}
