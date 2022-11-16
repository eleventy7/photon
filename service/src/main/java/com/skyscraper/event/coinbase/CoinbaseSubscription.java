package com.skyscraper.event.coinbase;

import com.skyscraper.enums.CoinbaseChannel;
import lombok.Getter;

@Getter
public class CoinbaseSubscription {
    public CoinbaseChannel name;
    public String[] product_ids;
}
