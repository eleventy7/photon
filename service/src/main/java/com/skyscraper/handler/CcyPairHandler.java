package com.skyscraper.handler;

import com.skyscraper.book.EnrichedOrder;
import com.skyscraper.book.OrderBook;
import com.skyscraper.book.OrderBookSide;
import com.skyscraper.config.FiatConversionConfig;
import com.skyscraper.config.VenueConfig;
import com.skyscraper.config.VenueList;
import com.skyscraper.enums.CcyPair;
import com.skyscraper.enums.Side;
import com.skyscraper.enums.Venue;
import com.skyscraper.prototypes.MarketPriceFlyweight;
import com.skyscraper.prototypes.OrderFlyweight;
import com.skyscraper.prototypes.ReconstructedBookFlyweight;
import com.skyscraper.prototypes.TradeFlyweight;
import lombok.extern.slf4j.Slf4j;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.concurrent.EpochMicroClock;
import org.agrona.concurrent.MessageHandler;
import org.agrona.concurrent.SystemEpochMicroClock;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CcyPairHandler implements MessageHandler {
    private final CcyPair ccyPair;
    private final EpochMicroClock usMicroClock = new SystemEpochMicroClock();
    final OrderFlyweight order = new OrderFlyweight();
    final TradeFlyweight trade = new TradeFlyweight();
    final MarketPriceFlyweight marketPrice = new MarketPriceFlyweight();
    private double fiatUsdt = Double.MIN_VALUE;
    private final OrderBook crossVenueOrderBook;
    private final OrderBook ftxOrderBook;
    private final OrderBook binanceOrderBook;
    private final OrderBook coinbaseOrderBook;
    private final ReconstructedBookFlyweight bookFlyweight;
    private final OneToOneRingBuffer ccyOutbound;
    private final Int2ObjectHashMap<VenueConfig> venueRepToConfig = new Int2ObjectHashMap<>();
    private final AtomicInteger currentLevel;

    public CcyPairHandler(CcyPair ccyPair, OneToOneRingBuffer ccyOutbound, VenueList venueList) {
        this.ccyPair = ccyPair;
        crossVenueOrderBook = new OrderBook(ccyPair);
        ftxOrderBook = new OrderBook(ccyPair, Venue.FTX);
        binanceOrderBook = new OrderBook(ccyPair, Venue.Binance);
        coinbaseOrderBook = new OrderBook(ccyPair, Venue.Coinbase);
        this.ccyOutbound = ccyOutbound;
        for (var venueConfig : venueList.getVenueConfigs()){
            venueRepToConfig.put(venueConfig.getVenue().getRepresentation(), venueConfig);
        }
        this.bookFlyweight = new ReconstructedBookFlyweight();
        this.currentLevel = new AtomicInteger(0);
    }

    @Override
    public void onMessage(int msgTypeId, MutableDirectBuffer buffer, int index, int length) {
        if (msgTypeId == OrderFlyweight.EIDER_ID) {
            order.setUnderlyingBuffer(buffer, index);
            final EnrichedOrder enrichedOrder = enrichOrder(order);
            if (order.readVenue() == Venue.FTX.getRepresentation()) {
                ftxOrderBook.acceptOrder(enrichedOrder);
                sendBookUpdate(ftxOrderBook, order.readVenue(), order.readSide(), ccyPair);
            } else if (order.readVenue() == Venue.Binance.getRepresentation()) {
                binanceOrderBook.acceptOrder(enrichedOrder);
                sendBookUpdate(binanceOrderBook, order.readVenue(), order.readSide(), ccyPair);
            } else if (order.readVenue() == Venue.Coinbase.getRepresentation()) {
                coinbaseOrderBook.acceptOrder(enrichedOrder);
                sendBookUpdate(coinbaseOrderBook, order.readVenue(), order.readSide(), ccyPair);
            }
            crossVenueOrderBook.acceptOrder(enrichedOrder);
        } else if (msgTypeId == TradeFlyweight.EIDER_ID) {
            trade.setUnderlyingBuffer(buffer, index);
            Venue venue = Venue.fromRepresentation(trade.readVenue());
            log.info("{} agent received {} trade {} ms behind market; {} μs overhead; price {}", ccyPair,
                    venue, trade.readReceiveTime() - trade.readTime(),
                    usMicroClock.microTime() - trade.readMicroReceiveTime(), trade.readPrice());
            if (FiatConversionConfig.requiresConversion(ccyPair)) {
                double price = trade.readPrice();
                if (fiatUsdt != Double.MIN_VALUE) {
                    double fiatPrice = fiatUsdt * price;
                    log.info("converted price {}",fiatPrice);
                }
            }
            if (trade.readVenue() == Venue.FTX.getRepresentation()) {
                ftxOrderBook.acceptTrade(trade);
            } else if (trade.readVenue() == Venue.Binance.getRepresentation()) {
                binanceOrderBook.acceptTrade(trade);
            } else if (trade.readVenue() == Venue.Coinbase.getRepresentation()) {
                coinbaseOrderBook.acceptTrade(trade);
            }
            crossVenueOrderBook.acceptTrade(trade);
        } else if (msgTypeId == MarketPriceFlyweight.EIDER_ID) {
            marketPrice.setUnderlyingBuffer(buffer, index);
            fiatUsdt = marketPrice.readPrice();
            log.info("{} agent received {} market price; {} μs overhead", ccyPair, marketPrice.readPrice(),
                usMicroClock.microTime() - marketPrice.readMicroReceiveTime());
        }
    }

    private void sendBookUpdate(OrderBook orderBook, short venue, short side, CcyPair ccyPair)
    {
        final OrderBookSide workingOrderBook;
        if (side == Side.buy.getRepresentation()) {
            workingOrderBook = orderBook.getBids();
        } else {
            workingOrderBook = orderBook.getAsks();
        }
        final int size = workingOrderBook.getAggregatedOrderBookByLevelAsTreeMap().size();
        final int requiredBytes = bookFlyweight.precomputeBufferLength(size);
        final int index = ccyOutbound.tryClaim(bookFlyweight.eiderId(), requiredBytes);
        if (index >= 0) {
            bookFlyweight.setUnderlyingBuffer(ccyOutbound.buffer(), index);
            bookFlyweight.resetPriceSizeRecordSize(size);
            bookFlyweight.writeSide(side);
            bookFlyweight.writeVenue(venue);
            bookFlyweight.writeCcyPair(ccyPair.getId());
            bookFlyweight.writeUpdatedMicros(usMicroClock.microTime());
            currentLevel.set(0);
            workingOrderBook.getAggregatedOrderBookByLevelAsTreeMap().forEach((levelPrice, levelSize) -> {
                bookFlyweight.getPriceSizeRecord(currentLevel.get()).writePrice(levelPrice);
                bookFlyweight.getPriceSizeRecord(currentLevel.getAndIncrement()).writeSize(levelSize);
            });
            ccyOutbound.commit(index);
        } else {
            log.error("{} agent failed to claim {} bytes from outbound buffer", ccyPair, requiredBytes);
        }
    }

    //not very efficient. at all.
    private EnrichedOrder enrichOrder(OrderFlyweight order) {
        final VenueConfig venueConfig = venueRepToConfig.get(order.readVenue());
        final short side = order.readSide();
        final double originalPrice = order.readPrice();
        double fiatPrice = originalPrice;
        double postFeePrice;

        if (FiatConversionConfig.requiresConversion(ccyPair) && fiatUsdt != Double.MIN_VALUE) {
            fiatPrice = fiatUsdt * originalPrice;
        }

        if (side == Side.buy.getRepresentation()) {
            postFeePrice = fiatPrice - (venueConfig.getFees().getTakerFeeBp() * fiatPrice);
        } else {
            postFeePrice = fiatPrice + (venueConfig.getFees().getMakerFeeBp() * fiatPrice);
        }

        return EnrichedOrder.builder()
            .sourcePrice(originalPrice)
            .size(order.readSize())
            .receiveTime(order.readReceiveTime())
            .microReceiveTime(order.readMicroReceiveTime())
            .side(side)
            .time(order.readTime())
            .venue(order.readVenue())
            .finalPrice(fiatPrice)
            .postFeePrice(postFeePrice)
            .build();
    }
}
