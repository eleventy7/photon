package com.skyscraper.book;

import java.util.TreeMap;
import com.skyscraper.prototypes.TradeFlyweight;

public class OrderBookSide
{
    private static final int MAX_LEVELS = 10;

    public OrderBookSide()
    {
        //removed
    }

    public void accept(TradeFlyweight trade)
    {
        //removed
    }

    public void accept(EnrichedOrder order)
    {
        //removed
    }

    public TreeMap<Double, Double> getAggregatedOrderBookByLevelAsTreeMap()
    {
        return null;
    }
}
