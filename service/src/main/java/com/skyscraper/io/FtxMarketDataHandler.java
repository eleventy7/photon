package com.skyscraper.io;

import com.dslplatform.json.DslJson;
import com.skyscraper.config.VenueConfig;
import com.skyscraper.enums.Venue;
import com.skyscraper.event.ftx.OrderPayload;
import com.skyscraper.event.ftx.Payload;
import com.skyscraper.event.ftx.TradePayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.concurrent.ringbuffer.ManyToOneRingBuffer;

@Slf4j
public class FtxMarketDataHandler extends BaseMarketDataHandler {
    private final Payload payload = new Payload();
    private final TradePayload ftxTradePayload = new TradePayload();
    private final OrderPayload ftxOrderPayload = new OrderPayload();

    public FtxMarketDataHandler(DslJson<Object> dslJson, VenueConfig venueConfig,
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
            switch (payload.getType()) {
                case update -> {
                    switch (payload.getChannel()) {
                        // re-process array as specific type :/
                        case trades -> {
                            reader.process(array, array.length).next(TradePayload.class, ftxTradePayload);
                            final int assetOrdinal = venueConfig.getStringToCcyPair().get(ftxTradePayload.getPair()).ordinal();
                            if (buffers.containsKey(assetOrdinal)) {
                                for (int i = 0; i < ftxTradePayload.getData().length; i++) {
                                    val ftxTrade = ftxTradePayload.getData()[i];
                                    processTrade(ftxTrade.getSide(), ftxTrade.getTime().toInstant().toEpochMilli(),
                                            ftxTrade.getPrice(), ftxTrade.getSize(), Venue.FTX, ftxTradePayload.getPair());
                                }
                            } else {
                                log.warn("unknown pair {}", ftxTradePayload.getPair());
                            }
                        }
                        case orderbook -> {
                            reader.process(array, array.length).next(OrderPayload.class, ftxOrderPayload);
                            processOrderPayload(ftxOrderPayload.getPair(), Venue.FTX, ftxOrderPayload.getData().getEpoch(),
                                    ftxOrderPayload.getData().getBids(), ftxOrderPayload.getData().getAsks());
                        }
                        case ticker -> log.info("Shouldn't be subscribed to ftx ticker");
                    }
                }
                case partial -> {
                    reader.process(array, array.length).next(OrderPayload.class, ftxOrderPayload);
                    processOrderPayload(ftxOrderPayload.getPair(), Venue.FTX, ftxOrderPayload.getData().getEpoch(),
                            ftxOrderPayload.getData().getBids(), ftxOrderPayload.getData().getAsks());
                }
                case info -> {
                    log.info("ftx info: {} {}", payload.getCode(), payload.getMessage());
                    if (20001 == payload.getCode()) {
                        log.info("ftx requested us to reconnect...");
                        // todo: reconnect websocket
                    }
                }
                case subscribed -> log.info("subscribed to {} {}", payload.getPair(), payload.getChannel());
                case unsubscribed -> log.info("unsubscribed from {} {}", payload.getPair(), payload.getChannel());
                case error -> log.error("ftx error {} {}", payload.getCode(), payload.getMessage());
                case pong -> log.debug("pong");
                default -> log.warn("Unhandled ftx event {}", payload);
            }
        } catch (Exception e) {
            log.error("problem in channelRead0: {}", e.toString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("problem in FtxMarketDataHandler: {}", cause.getLocalizedMessage());
        ctx.close();
    }
}
