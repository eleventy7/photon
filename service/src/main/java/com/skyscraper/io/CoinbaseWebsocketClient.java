package com.skyscraper.io;

import com.dslplatform.json.DslJson;
import com.skyscraper.command.CoinbaseRequest;
import com.skyscraper.command.CoinbaseSubscriptionGroup;
import com.skyscraper.enums.CoinbaseChannel;
import com.skyscraper.enums.CoinbaseRequestType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Slf4j
public class CoinbaseWebsocketClient extends WebsocketClient {
    private final DslJson<Object> dslJson;

    public CoinbaseWebsocketClient(String url, BaseMarketDataHandler handler, DslJson<Object> dslJson) {
        super(url, handler);
        this.dslJson = dslJson;
        setMaxFramePayloadLength(65536 * 100); // todo: revisit this size
    }

    @Override
    public void subscribe(List<String> pairs) {
        val channels = List.of(CoinbaseChannel.level2, CoinbaseChannel.matches);
        val pair2Asset = getVenueConfig().getStringToCcyPair();
        List<CoinbaseSubscriptionGroup> groups = new ArrayList<>();
        for (val channel : channels) {
            var builder = CoinbaseSubscriptionGroup.builder().name(channel);
            var productIds = channel == CoinbaseChannel.level2 ?
                    pairs.stream().filter(p -> !pair2Asset.get(p).isReferencePrice()).collect(Collectors.toList()) :
                    pairs;
            groups.add(builder.productIds(productIds).build());
            log.info("Subscribing to {} for {}", channel, productIds);
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.reset();
        try {
            dslJson.serialize(CoinbaseRequest.builder()
                    .type(CoinbaseRequestType.subscribe)
                    .channels(groups)
                    .build(), os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send(os);
    }

    @Override
    public void heartbeat(ScheduledExecutorService heartbeatExecutorService) {
    }
}
