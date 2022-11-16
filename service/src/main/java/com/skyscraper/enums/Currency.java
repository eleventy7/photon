package com.skyscraper.enums;

import lombok.Getter;

@Getter
public enum Currency {
    // fiat
    USD(false),

    // stables
    USDT(true),
    USDC(true),
    DAI(true),

    // crypto
    BTC(false),
    ETH(false);

    private final boolean stableCoin;

    Currency(boolean stableCoin) {
        this.stableCoin = stableCoin;
    }
}
