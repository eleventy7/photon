package com.skyscraper.io;

import com.dslplatform.json.DslJson;
import com.skyscraper.config.VenueConfig;
import com.skyscraper.enums.Venue;
import com.skyscraper.event.coinbase.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;

@Slf4j
public class CoinbaseMarketDataHandler extends BaseMarketDataHandler {
    private final Payload payload = new Payload();
    private final SubscriptionPayload coinbaseSubscriptionPayload = new SubscriptionPayload();
    private final L2Snapshot coinbaseL2Snapshot = new L2Snapshot();
    private final L2Update coinbaseL2Update = new L2Update();
    private final Match coinbaseMatch = new Match();

    public CoinbaseMarketDataHandler(DslJson<Object> dslJson, VenueConfig venueConfig,
                                     Int2ObjectHashMap<ManyToOneRingBuffer> buffers) {
        this.reader = dslJson.newReader();
        this.venueConfig = venueConfig;
        this.buffers = buffers;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        try {
            // get backing byte array
            final byte[] array;
            if (byteBuf.hasArray()) {
                array = byteBuf.array();
            } else {
                array = threadLocalTempArray(byteBuf.readableBytes());
                byteBuf.getBytes(byteBuf.readerIndex(), array, 0, byteBuf.readableBytes());
            }
            reader.process(array, array.length).next(Payload.class, payload);
            switch (payload.getType()) {
                case subscriptions -> {
                    reader.process(array, array.length).next(SubscriptionPayload.class, coinbaseSubscriptionPayload);
                    for (val subscription : coinbaseSubscriptionPayload.channels) {
                        log.info("subscribed to {} for {}", subscription.getName(), String.join(",", subscription.getProduct_ids()));
                    }
                }
                case snapshot -> {
                    reader.process(array, array.length).next(L2Snapshot.class, coinbaseL2Snapshot);
                    processOrderPayload(coinbaseL2Snapshot.getPair(), Venue.Coinbase, clock.time(),
                            coinbaseL2Snapshot.getBids(), coinbaseL2Snapshot.getAsks());
                }
                case l2update -> {
                    reader.process(array, array.length).next(L2Update.class, coinbaseL2Update);
                    processOrderChanges(coinbaseL2Update.getPair(), Venue.Coinbase,
                            coinbaseL2Update.getTime().toInstant().toEpochMilli(), coinbaseL2Update.getChanges());
                }
                case match, last_match -> { // todo: make separate block to handle last_match if we need to differentiate
                    reader.process(array, array.length).next(Match.class, coinbaseMatch);
                    if (venueConfig.getStringToCcyPair().get(coinbaseMatch.getPair()).isReferencePrice()) {
                        processMarketPrice(coinbaseMatch.getTime().toInstant().toEpochMilli(), coinbaseMatch.getPrice(),
                                coinbaseMatch.getPair());
                    } else {
                        processTrade(coinbaseMatch.getMakerSide().getOpposite(), coinbaseMatch.getTime().toInstant().toEpochMilli(),
                                coinbaseMatch.getPrice(), coinbaseMatch.getSize(), Venue.Coinbase, coinbaseMatch.getPair());
                    }
                }
                default -> log.warn("unsupported coinbase message type: {}", payload.getType());
            }
        } catch (Exception e) {
            log.error("problem in channelRead0: {}", e.toString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("problem in CoinbaseMarketDataHandler: {}", cause.getLocalizedMessage());
        ctx.close();
    }
}
