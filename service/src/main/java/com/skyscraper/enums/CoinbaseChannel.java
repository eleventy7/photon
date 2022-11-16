package com.skyscraper.enums;

public enum CoinbaseChannel {
    heartbeat,
    status,
    ticker,
    level2,
    matches
    // there are more here: https://docs.cloud.coinbase.com/exchange/docs/channels
    // but we likely don't need them now
}