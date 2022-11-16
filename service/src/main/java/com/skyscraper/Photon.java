package com.skyscraper;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.skyscraper.bookconsumer.BookConsumerAgent;
import com.skyscraper.bookconsumer.BookConsumerHandler;
import com.skyscraper.config.CcyPairList;
import com.skyscraper.config.VenueConfig;
import com.skyscraper.config.VenueList;
import com.skyscraper.enums.CcyPair;
import com.skyscraper.handler.CcyPairAgent;
import com.skyscraper.handler.CcyPairHandler;
import com.skyscraper.infra.PhotonErrorHandler;
import com.skyscraper.infra.RingBufferBuilder;
import com.skyscraper.io.*;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.YieldingIdleStrategy;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Slf4j
public class Photon {
    private static void run(String[] args) throws Exception {
        // init json and datadog
        final DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());
        StatsDClient statsd = new NonBlockingStatsDClientBuilder()
                .prefix("statsd")
                .hostname("localhost")
                .port(8125)
                .build();

        final int agronaBufferSize = 8 * 1024 * 1024;
        final Int2ObjectHashMap<ManyToOneRingBuffer> ringBuffers = new Int2ObjectHashMap<>();
        final Int2ObjectHashMap<ManyToOneRingBuffer> agentBuffers = new Int2ObjectHashMap<>();
        final Int2ObjectHashMap<AgentRunner> agentRunners = new Int2ObjectHashMap<>();
        final List<OneToOneRingBuffer> outputRingBuffers = new ArrayList<>();

        // read venue and pair config
        var mapper = new ObjectMapper(new YAMLFactory());
        VenueList venueList = mapper.readValue(new File("service/src/main/resources/venue_config.yaml"), VenueList.class);
        CcyPairList ccyPairList = mapper.readValue(new File("service/src/main/resources/pair_config.yaml"), CcyPairList.class);

        log.info("Setting up agents...");
        for (final CcyPair ccyPair : ccyPairList.getCcyPairs()) {
            if (!ccyPair.isReferencePrice()) {
                final ManyToOneRingBuffer venueInbound = RingBufferBuilder.getManyToOneRingBuffer(agronaBufferSize, true);
                ringBuffers.put(ccyPair.ordinal(), venueInbound);
                final OneToOneRingBuffer ccyOutbound = RingBufferBuilder.getOneToOneRingBuffer(agronaBufferSize, true);
                outputRingBuffers.add(ccyOutbound);
                final CcyPairAgent agent = new CcyPairAgent(ccyPair, new CcyPairHandler(ccyPair, ccyOutbound, venueList), venueInbound);
                final AgentRunner runner = new AgentRunner(getDefaultIdleStrategy(), PhotonErrorHandler.INSTANCE, null, agent);
                AgentRunner.startOnThread(runner);
                agentBuffers.put(ccyPair.ordinal(), venueInbound);
                agentRunners.put(ccyPair.ordinal(), runner);
            }
        }

        final BookConsumerHandler bookConsumerHandler = new BookConsumerHandler();
        final BookConsumerAgent bookConsumerAgent = new BookConsumerAgent(outputRingBuffers, bookConsumerHandler);
        final AgentRunner bookConsumerRunner = new AgentRunner(getDefaultIdleStrategy(), PhotonErrorHandler.INSTANCE, null, bookConsumerAgent);
        AgentRunner.startOnThread(bookConsumerRunner);

        // heartbeat threadpool
        ScheduledExecutorService heartbeatExecutorService = Executors.newScheduledThreadPool(2);

        // connect to each venue and subscribe to market data
        for (final VenueConfig venueConfig : venueList.getVenueConfigs()) {
            if (Objects.isNull(venueConfig.getVenue())) {
                continue;
            }
            venueConfig.updateMaps();
            final var pairs = ccyPairList.getCcyPairs().stream()
                    .map(asset -> venueConfig.getCcyPairToString().get(asset))
                    .filter(Objects::nonNull)
                    .toList();

            log.info("Connecting to {}...", venueConfig.getVenue());

            WebsocketClient wsClient;
            switch (venueConfig.getVenue()) {
                case FTX -> {
                    var handler = new FtxMarketDataHandler(dslJson, venueConfig, agentBuffers);
                    wsClient = new FtxWebsocketClient(venueConfig.getUrl(), handler, dslJson);
                }
                case Binance -> {
                    var handler = new BinanceMarketDataHandler(dslJson, venueConfig, agentBuffers);
                    String url = venueConfig.getUrl() +
                            pairs.stream().map(pair -> {
                                        final String lcPair = pair.toLowerCase(Locale.ROOT);
                                        return lcPair + "@trade/" + lcPair + "@depth@100ms";
                                    })
                                    .collect(Collectors.joining());
                    wsClient = new BinanceWebsocketClient(url, handler);
                }
                case Coinbase -> {
                    var handler = new CoinbaseMarketDataHandler(dslJson, venueConfig, agentBuffers);
                    wsClient = new CoinbaseWebsocketClient(venueConfig.getUrl(), handler, dslJson);
                }
                default -> {
                    log.warn("unknown venue {}", venueConfig.getVenue());
                    continue;
                }
            }
            wsClient.connect();
            wsClient.login();
            wsClient.heartbeat(heartbeatExecutorService);
            wsClient.subscribe(pairs);
        }
    }

    public static void main(String[] args) throws Exception {
        run(args);
    }

    private static IdleStrategy getDefaultIdleStrategy() {
        //if CPUs dying of overload, swap to something like SleepingMillis. Could make it configurable.
        //to reduce internal overheads, go with NoOpIdleStrategy or BusySpinIdleStrategy. This can slow things on
        //a system with insufficient cores.
        return new YieldingIdleStrategy();
    }
}
