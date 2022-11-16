package com.skyscraper.enums;

import lombok.Getter;

@Getter
public enum CcyPair {
    BTCUSD(false, Currency.BTC, Currency.USD, (short) 0),
    BTCUSDT(false, Currency.BTC, Currency.USDT, (short) 1),
    ETHUSD(false, Currency.ETH, Currency.USD, (short) 2),
    ETHUSDT(false, Currency.ETH, Currency.USDT, (short) 3),
    USDUSDT(true, Currency.USD, Currency.USDT, (short) 4);

    private final boolean referencePrice;
    private final Currency quote;
    private final Currency base;
    private final short id;

    CcyPair(boolean referencePrice, Currency quote, Currency base, short id) {
        this.referencePrice = referencePrice;
        this.quote = quote;
        this.base = base;
        this.id = id;
    }

    public boolean isReferencePrice() {
        return referencePrice;
    }

    public Currency getQuote() {
        return quote;
    }

    public Currency getBase() {
        return base;
    }

    public short getId() {
        return id;
    }
}
