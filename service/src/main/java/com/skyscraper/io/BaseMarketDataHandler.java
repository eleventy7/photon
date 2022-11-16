package com.skyscraper.io;

import com.dslplatform.json.JsonReader;
import com.skyscraper.config.VenueConfig;
import com.skyscraper.enums.Side;
import com.skyscraper.enums.Venue;
import com.skyscraper.event.ExchangeOrder;
import com.skyscraper.event.ExchangeOrderChange;
import com.skyscraper.prototypes.MarketPriceFlyweight;
import com.skyscraper.prototypes.OrderFlyweight;
import com.skyscraper.prototypes.TradeFlyweight;
import io.netty.buffer.ByteBuf;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.PlatformDependent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.concurrent.EpochClock;
import org.agrona.concurrent.EpochMicroClock;
import org.agrona.concurrent.SystemEpochClock;
import org.agrona.concurrent.SystemEpochMicroClock;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;

@Slf4j
public abstract class BaseMarketDataHandler extends SimpleChannelInboundHandler<ByteBuf> {
    final EpochClock clock = SystemEpochClock.INSTANCE;
    final EpochMicroClock usClock = new SystemEpochMicroClock();
    final OrderFlyweight order = new OrderFlyweight();
    final TradeFlyweight trade = new TradeFlyweight();
    final MarketPriceFlyweight marketPrice = new MarketPriceFlyweight();

    private final FastThreadLocal<byte[]> byteArrays = new FastThreadLocal<>() {
        @Override
        protected byte[] initialValue() {
            return PlatformDependent.allocateUninitializedArray(1024);
        }
    };
    Int2ObjectHashMap<ManyToOneRingBuffer> buffers;
    JsonReader<Object> reader;
    @Getter
    VenueConfig venueConfig;

    byte[] threadLocalTempArray(int minLength) {
        return minLength <= 1024 ? byteArrays.get()
                : PlatformDependent.allocateUninitializedArray(minLength);
    }

    private void writeOrders(String pair, Venue venue, long orderTime, Side side, ExchangeOrder[] orders,
                             int assetOrdinal) {
        for (int i = 0; i < orders.length; i++) {
            final ExchangeOrder exchangeOrder = orders[i];
            int claimedIndex = buffers.get(assetOrdinal).tryClaim(OrderFlyweight.EIDER_ID, OrderFlyweight.BUFFER_LENGTH);
            if (claimedIndex > 0) {
                order.setUnderlyingBuffer(buffers.get(assetOrdinal).buffer(), claimedIndex);
                //order of writes is consistent with order of fields in OrderFlyweight to stride through buffer
                order.writeSide(side.getRepresentation());
                order.writeTime(orderTime);
                order.writePrice(exchangeOrder.getPrice());
                order.writeSize(exchangeOrder.getSize());
                order.writeVenue(venue.getRepresentation());
                order.writeReceiveTime(clock.time());
                order.writeMicroReceiveTime(usClock.microTime());
                buffers.get(assetOrdinal).commit(claimedIndex);
            } else {
                log.error("could not claim {} bytes in the ringbuffer for pair {}", OrderFlyweight.BUFFER_LENGTH, pair);
            }
        }
    }

    void processOrderPayload(String pair, Venue venue, long orderTime, ExchangeOrder[] bids, ExchangeOrder[] asks) {
        try {
            final int assetOrdinal = venueConfig.getStringToCcyPair().get(pair).ordinal();
            if (buffers.containsKey(assetOrdinal)) {
                writeOrders(pair, venue, orderTime, Side.buy, bids, assetOrdinal);
                writeOrders(pair, venue, orderTime, Side.sell, asks, assetOrdinal);
            } else {
                log.warn("unknown pair {}", pair);
            }
        } catch (Exception e) {
            log.error("problem processing order payload: {}", e.toString());
        }
    }

    void processOrderChanges(String pair, Venue venue, long orderTime, ExchangeOrderChange[] orders) {
        final int assetOrdinal = venueConfig.getStringToCcyPair().get(pair).ordinal();
        if (buffers.containsKey(assetOrdinal)) {
            for (int i = 0; i < orders.length; i++) {
                final ExchangeOrderChange exchangeOrderChange = orders[i];
                int claimedIndex = buffers.get(assetOrdinal).tryClaim(OrderFlyweight.EIDER_ID, OrderFlyweight.BUFFER_LENGTH);
                if (claimedIndex > 0) {
                    order.setUnderlyingBuffer(buffers.get(assetOrdinal).buffer(), claimedIndex);
                    //order of writes is consistent with order of fields in OrderFlyweight to stride through buffer
                    order.writeSide(exchangeOrderChange.getSide().getRepresentation());
                    order.writeTime(orderTime);
                    order.writePrice(exchangeOrderChange.getPrice());
                    order.writeSize(exchangeOrderChange.getSize());
                    order.writeVenue(venue.getRepresentation());
                    order.writeReceiveTime(clock.time());
                    order.writeMicroReceiveTime(usClock.microTime());
                    buffers.get(assetOrdinal).commit(claimedIndex);
                } else {
                    log.error("could not claim {} bytes in the ringbuffer for pair {}", OrderFlyweight.BUFFER_LENGTH, pair);
                }
            }
        } else {
            log.warn("unknown pair {}", pair);
        }
    }

    void processTrade(Side side, long tradeTime, double price, double size, Venue venue, String pair) {
        try {
            final int assetOrdinal = venueConfig.getStringToCcyPair().get(pair).ordinal();
            if (buffers.containsKey(assetOrdinal)) {
                int claimedIndex = buffers.get(assetOrdinal).tryClaim(TradeFlyweight.EIDER_ID, TradeFlyweight.BUFFER_LENGTH);
                if (claimedIndex > 0) {
                    trade.setUnderlyingBuffer(buffers.get(assetOrdinal).buffer(), claimedIndex);
                    trade.writeSide(side.getRepresentation());
                    trade.writeTime(tradeTime);
                    trade.writePrice(price);
                    trade.writeSize(size);
                    trade.writeVenue(venue.getRepresentation());
                    trade.writeReceiveTime(clock.time());
                    trade.writeMicroReceiveTime(usClock.microTime());
                    buffers.get(assetOrdinal).commit(claimedIndex);
                } else {
                    log.error("could not claim {} bytes in the ringbuffer for pair {}", TradeFlyweight.BUFFER_LENGTH, pair);
                }
            } else {
                log.warn("unknown pair {}", pair);
            }
        } catch (Exception e) {
            log.error("problem processing trade: {}", e.toString());
        }
    }

    void processMarketPrice(long tradeTime, double price, String pair) {
        try {
            for (val buffer : buffers.values()) {
                int claimedIndex = buffer.tryClaim(MarketPriceFlyweight.EIDER_ID, MarketPriceFlyweight.BUFFER_LENGTH);
                if (claimedIndex > 0) {
                    marketPrice.setUnderlyingBuffer(buffer.buffer(), claimedIndex);
                    marketPrice.writeTime(tradeTime);
                    marketPrice.writePrice(price);
                    marketPrice.writeReceiveTime(clock.time());
                    marketPrice.writeMicroReceiveTime(usClock.microTime());
                    buffer.commit(claimedIndex);
                } else {
                    log.error("could not claim {} bytes in the ringbuffer for pair {}", MarketPriceFlyweight.BUFFER_LENGTH, pair);
                }
            }
        } catch (Exception e) {
            log.error("problem processing trade: {}", e.toString());
        }
    }
}
