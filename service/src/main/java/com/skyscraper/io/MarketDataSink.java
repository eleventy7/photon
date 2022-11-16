package com.skyscraper.io;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public interface MarketDataSink {
    void connect() throws Exception;

    void disconnect();

    default void login() {
    }

    void subscribe(List<String> pairs);

    void heartbeat(ScheduledExecutorService heartbeatExecutorService);
}
