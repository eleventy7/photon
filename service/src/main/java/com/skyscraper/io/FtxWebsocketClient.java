package com.skyscraper.io;

import com.dslplatform.json.DslJson;
import com.skyscraper.command.FtxPing;
import com.skyscraper.command.FtxRequest;
import com.skyscraper.enums.FtxChannel;
import com.skyscraper.enums.FtxOp;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FtxWebsocketClient extends WebsocketClient {
    private final DslJson<Object> dslJson;

    public FtxWebsocketClient(String url, BaseMarketDataHandler handler, DslJson<Object> dslJson) {
        super(url, handler);
        this.dslJson = dslJson;
    }

    @Override
    public void login() {
        // todo: auth not needed right now, uncomment below when submitting orders
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        try {
//            FtxAuth ftxAuth = new FtxAuth();
//            ftxAuth.encodeArgs(getVenueConfig().getApiKey(), getVenueConfig().getSecretKey());
//            dslJson.serialize(ftxAuth, os);
//            send(os);
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
    }

    @Override
    public void subscribe(List<String> pairs) {
        final var channels = List.of(FtxChannel.orderbook, FtxChannel.trades);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        pairs.forEach(
                pair -> channels.forEach(ch -> {
                    os.reset();
                    try {
                        log.info("Subscribing to {} for {}", ch, pair);
                        dslJson.serialize(FtxRequest.builder().op(FtxOp.subscribe).channel(ch).market(pair).build(), os);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    send(os);
                }));
    }

    @Override
    public void heartbeat(ScheduledExecutorService heartbeatExecutorService) {
        ByteArrayOutputStream ping = new ByteArrayOutputStream();
        try {
            dslJson.serialize(new FtxPing(), ping);
            final byte[] pingBytes = ping.toByteArray();
            heartbeatExecutorService.scheduleAtFixedRate(() -> send(pingBytes), 0, 15, TimeUnit.SECONDS);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
