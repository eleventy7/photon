package com.skyscraper;

import com.skyscraper.enums.Side;
import com.skyscraper.enums.Venue;
import com.skyscraper.infra.RingBufferBuilder;
import com.skyscraper.internal.MarketPrice;
import com.skyscraper.internal.Order;
import com.skyscraper.internal.Trade;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.ringbuffer.OneToOneRingBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerializerTest
{
    private Order lastObservedOrder;
    private Trade lastObservedTrade;
    private MarketPrice lastObservedMarketPrice;

    @Test
    void tradeCorrectOverBufferCopy()
    {
        final OneToOneRingBuffer buffer = RingBufferBuilder.getOneToOneRingBuffer(1024, true);
        final Trade toMove = sampleTrade();
        final ExpandableArrayBuffer copyBuffer = new ExpandableArrayBuffer(); //can run copy free with tryClaim
        toMove.writeToByteBuffer(copyBuffer, 0);
        buffer.write(Trade.MESSAGE_TYPE, copyBuffer, 0, Trade.MESSAGE_LENGTH);
        lastObservedTrade = null;
        buffer.read(this::consume);
        assertNotNull(lastObservedTrade);
        assertEquals(toMove.getSide(), lastObservedTrade.getSide());
        assertEquals(toMove.getTime(), lastObservedTrade.getTime());
        assertEquals(toMove.getPrice(), lastObservedTrade.getPrice());
        assertEquals(toMove.getSize(), lastObservedTrade.getSize());
        assertEquals(toMove.getReceiveTime(), lastObservedTrade.getReceiveTime());
        assertEquals(toMove.getVenue(), lastObservedTrade.getVenue());
        assertEquals(toMove.getPair(), lastObservedTrade.getPair());
    }

    @Test
    void marketPriceCorrectOverBufferCopy()
    {
        final OneToOneRingBuffer buffer = RingBufferBuilder.getOneToOneRingBuffer(1024, true);
        final MarketPrice toMove = sampleMarketPrice();
        final ExpandableArrayBuffer copyBuffer = new ExpandableArrayBuffer(); //can run copy free with tryClaim
        toMove.writeToByteBuffer(copyBuffer, 0);
        buffer.write(MarketPrice.MESSAGE_TYPE, copyBuffer, 0, MarketPrice.MESSAGE_LENGTH);
        lastObservedMarketPrice = null;
        buffer.read(this::consume);
        assertNotNull(lastObservedMarketPrice);
        assertEquals(toMove.getSide(), lastObservedMarketPrice.getSide());
        assertEquals(toMove.getPair(), lastObservedMarketPrice.getPair());
        assertEquals(toMove.getTime(), lastObservedMarketPrice.getTime());
        assertEquals(toMove.getReceiveTime(), lastObservedMarketPrice.getReceiveTime());
        assertEquals(toMove.getVenue(), lastObservedMarketPrice.getVenue());
        assertEquals(toMove.getPrice(), lastObservedMarketPrice.getPrice());
    }

    @Test
    void orderCorrectOverBufferCopy()
    {
        final OneToOneRingBuffer buffer = RingBufferBuilder.getOneToOneRingBuffer(1024, true);
        final Order toMove = sampleOrder();
        final ExpandableArrayBuffer copyBuffer = new ExpandableArrayBuffer(); //can run copy free with tryClaim
        toMove.writeToByteBuffer(copyBuffer, 0);
        buffer.write(Order.MESSAGE_TYPE, copyBuffer, 0, Order.MESSAGE_LENGTH);
        lastObservedOrder = null;
        buffer.read(this::consume);
        assertNotNull(lastObservedOrder);
        assertEquals(toMove.getSide(), lastObservedOrder.getSide());
        assertEquals(toMove.getPair(), lastObservedOrder.getPair());
        assertEquals(toMove.getTime(), lastObservedOrder.getTime());
        assertEquals(toMove.getReceiveTime(), lastObservedOrder.getReceiveTime());
        assertEquals(toMove.getVenue(), lastObservedOrder.getVenue());
        assertEquals(toMove.getPrice(), lastObservedOrder.getPrice());
        assertEquals(toMove.getSize(), lastObservedOrder.getSize());
    }

    @Test
    void orderCorrectOverBufferZeroRingBufferCopy()
    {
        final OneToOneRingBuffer buffer = RingBufferBuilder.getOneToOneRingBuffer(1024, true);
        final Order toMove = sampleOrder();
        int interalBufferOffset = buffer.tryClaim(Order.MESSAGE_TYPE, Order.MESSAGE_LENGTH);
        assertTrue(interalBufferOffset > 0);
        toMove.writeToByteBuffer(buffer.buffer(), interalBufferOffset);
        buffer.commit(interalBufferOffset);
        lastObservedOrder = null;
        buffer.read(this::consume);
        assertNotNull(lastObservedOrder);
        assertEquals(toMove.getSide(), lastObservedOrder.getSide());
        assertEquals(toMove.getPair(), lastObservedOrder.getPair());
        assertEquals(toMove.getReceiveTime(), lastObservedOrder.getReceiveTime());
        assertEquals(toMove.getVenue(), lastObservedOrder.getVenue());
        assertEquals(toMove.getTime(), lastObservedOrder.getTime());
        assertEquals(toMove.getPrice(), lastObservedOrder.getPrice());
        assertEquals(toMove.getSize(), lastObservedOrder.getSize());
    }

    private void consume(int messageType, MutableDirectBuffer mutableDirectBuffer, int offset, int length)
    {
        if (messageType == Order.MESSAGE_TYPE)
        {
            lastObservedOrder = Order.fromDirectBuffer(mutableDirectBuffer, offset);
        }
        else if (messageType == Trade.MESSAGE_TYPE)
        {
            lastObservedTrade = Trade.fromDirectBuffer(mutableDirectBuffer, offset);
        }
        else if (messageType == MarketPrice.MESSAGE_TYPE)
        {
            lastObservedMarketPrice = MarketPrice.fromDirectBuffer(mutableDirectBuffer, offset);
        }
    }

    private Order sampleOrder()
    {
        final Order sample = new Order();
        sample.setPair("USDT/USD");
        sample.setSize(101);
        sample.setPrice(103);
        sample.setTime(10000L);
        sample.setSide(Side.buy);
        sample.setVenue(Venue.FTX);
        sample.setReceiveTime(10042L);
        return sample;
    }

    private MarketPrice sampleMarketPrice()
    {
        final MarketPrice sample = new MarketPrice();
        sample.setPair("USDT/USD");
        sample.setPrice(103);
        sample.setTime(10000L);
        sample.setSide(Side.buy);
        sample.setVenue(Venue.FTX);
        sample.setReceiveTime(10042L);
        return sample;
    }

    private Trade sampleTrade()
    {
        final Trade sample = new Trade();
        sample.setPair("USDT/USD");
        sample.setSize(101);
        sample.setPrice(103);
        sample.setTime(10000L);
        sample.setReceiveTime(10200L);
        sample.setSide(Side.buy);
        sample.setVenue(Venue.Binance);
        return sample;
    }

}
