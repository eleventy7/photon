package com.skyscraper.io;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class BinanceWebsocketClient extends WebsocketClient {

    public BinanceWebsocketClient(String url, BaseMarketDataHandler handler) {
        super(url, handler);
    }

    @Override
    public void subscribe(List<String> pairs) {
    }

    @Override
    public void heartbeat(ScheduledExecutorService heartbeatExecutorService) {
    }
}
