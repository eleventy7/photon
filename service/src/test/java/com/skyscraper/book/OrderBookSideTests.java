package com.skyscraper.book;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBookSideTests
{
    @Disabled
    void shouldAcceptOneOrder()
    {
        final OrderBookSide side = new OrderBookSide();
        side.accept(buildSampleOrder(1.0, 2.0));
        assertEquals(2.0, side.getAggregatedOrderBookByLevelAsTreeMap().get(1.0));
    }

    @Disabled
    void shouldAcceptMoreThanMaxLevels()
    {
        final OrderBookSide side = new OrderBookSide();
        side.accept(buildSampleOrder(1.0, 1.0));
        side.accept(buildSampleOrder(2.0, 2.0));
        side.accept(buildSampleOrder(3.0, 3.0));
        side.accept(buildSampleOrder(4.0, 4.0));
        side.accept(buildSampleOrder(5.0, 5.0));
        side.accept(buildSampleOrder(6.0, 6.0));
        side.accept(buildSampleOrder(7.0, 7.0));
        side.accept(buildSampleOrder(8.0, 8.0));
        side.accept(buildSampleOrder(9.0, 9.0));
        side.accept(buildSampleOrder(10.0, 10.0));
        side.accept(buildSampleOrder(11.0, 11.0));
        assertEquals(10, side.getAggregatedOrderBookByLevelAsTreeMap().size());
        assertEquals(1.0, side.getAggregatedOrderBookByLevelAsTreeMap().firstEntry().getValue());
        assertEquals(10.0, side.getAggregatedOrderBookByLevelAsTreeMap().lastEntry().getValue());
    }

    @Disabled
    void shouldRemoveWhenZeroSize()
    {
        final OrderBookSide side = new OrderBookSide();
        side.accept(buildSampleOrder(1.0, 1.0));
        side.accept(buildSampleOrder(2.0, 0.0));
        side.accept(buildSampleOrder(3.0, 3.0));
        assertEquals(2, side.getAggregatedOrderBookByLevelAsTreeMap().size());
        assertEquals(1.0, side.getAggregatedOrderBookByLevelAsTreeMap().firstEntry().getValue());
        assertEquals(3.0, side.getAggregatedOrderBookByLevelAsTreeMap().lastEntry().getValue());
    }

    private EnrichedOrder buildSampleOrder(final Double price, final Double size)
    {
        return EnrichedOrder.builder()
            .finalPrice(price)
            .size(size)
            .build();
    }
}
