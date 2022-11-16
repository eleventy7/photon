package com.skyscraper.io;

import com.dslplatform.json.DslJson;
import com.skyscraper.config.VenueConfig;
import com.skyscraper.enums.Side;
import com.skyscraper.enums.Venue;
import com.skyscraper.event.binance.BookDepthPayload;
import com.skyscraper.event.binance.BookUpdate;
import com.skyscraper.event.binance.Payload;
import com.skyscraper.event.binance.TradePayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;

@Slf4j
public class BinanceMarketDataHandler extends BaseMarketDataHandler {
    private final Payload payload = new Payload();
    private final BookDepthPayload bookDepthPayload = new BookDepthPayload();
    private final TradePayload binanceTradePayload = new TradePayload();

    public BinanceMarketDataHandler(DslJson<Object> dslJson, VenueConfig venueConfig,
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
            // bind byte[] and get next token
            reader.process(array, array.length).next(Payload.class, payload);
            // array of pair and stream
            final String[] arr = payload.getStream().split("@");
            final String pair = arr[0];
            final int assetOrdinal = venueConfig.getStringToCcyPair().get(pair).ordinal();
            final String stream = arr[1];
            switch (stream) {
                case "depth" -> {
                    reader.process(array, array.length).next(BookDepthPayload.class, bookDepthPayload);
                    final BookUpdate bookUpdate = bookDepthPayload.getData();
                    processOrderPayload(pair, Venue.Binance, bookUpdate.getEventTime(), bookUpdate.getBids(),
                            bookUpdate.getAsks());
                }
                case "trade" -> {
                    reader.process(array, array.length).next(TradePayload.class, binanceTradePayload);
                    if (buffers.containsKey(assetOrdinal)) {
                        val binanceTrade = binanceTradePayload.getData();
                        processTrade(binanceTrade.isBuyerMarketMaker() ? Side.sell : Side.buy,
                                binanceTrade.getTradeTime(), binanceTrade.getPrice(), binanceTrade.getSize(),
                                Venue.Binance, pair);
                    } else {
                        log.warn("unknown pair {}", pair);
                    }
                }
                default -> log.warn("unhandled binance stream {}", stream);
            }
        } catch (Exception e) {
            log.error("problem in channelRead0: {}", e.toString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("problem in BinanceMarketDataHandler: {}", cause.getLocalizedMessage());
        ctx.close();
    }
}
