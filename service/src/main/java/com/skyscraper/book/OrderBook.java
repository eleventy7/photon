package com.skyscraper.book;

import com.skyscraper.enums.CcyPair;
import com.skyscraper.enums.Side;
import com.skyscraper.enums.Venue;
import com.skyscraper.prototypes.TradeFlyweight;

public class OrderBook
{
    private final CcyPair ccyPair;
    private final Venue venue;

    private final OrderBookSide bids;
    private final OrderBookSide asks;

    public OrderBook(CcyPair ccyPair, Venue venue) {
        this.ccyPair = ccyPair;
        this.venue = venue;

        bids = new OrderBookSide();
        asks = new OrderBookSide();
    }

    public OrderBook(CcyPair ccyPair) {
        this.ccyPair = ccyPair;
        this.venue = null;

        bids = new OrderBookSide();
        asks = new OrderBookSide();
    }

    public CcyPair getAsset()
    {
        return ccyPair;
    }

    public Venue getVenue()
    {
        return venue;
    }

    public OrderBookSide getBids()
    {
        return bids;
    }

    public OrderBookSide getAsks()
    {
        return asks;
    }

    public void acceptTrade(TradeFlyweight trade)
    {
        if (trade.readSide() == Side.buy.getRepresentation()) {
            bids.accept(trade);
        } else {
            asks.accept(trade);
        }
    }

    public void acceptOrder(EnrichedOrder order)
    {
        if (order.getSide() == Side.buy.getRepresentation()) {
            bids.accept(order);
        } else {
            asks.accept(order);
        }
    }

}
