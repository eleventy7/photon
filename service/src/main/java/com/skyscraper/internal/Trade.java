package com.skyscraper.internal;

import com.skyscraper.enums.Side;
import com.skyscraper.enums.Venue;
import lombok.Data;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

@Data
public class Trade {
    public static final int MESSAGE_TYPE = MessageTypes.TRADE;
    private static final int SIDE_OFFSET = 0;
    private static final int TIME_OFFSET = SIDE_OFFSET + Byte.BYTES;
    private static final int PRICE_OFFSET = TIME_OFFSET + Long.BYTES;
    private static final int SIZE_OFFSET = PRICE_OFFSET + Double.BYTES;
    private static final int VENUE_OFFSET = SIZE_OFFSET + Double.BYTES;
    private static final int RECEIVE_TIME_OFFSET = VENUE_OFFSET + Integer.BYTES;
    private static final int PAIR_OFFSET = RECEIVE_TIME_OFFSET + Long.BYTES;
    public static final int MESSAGE_LENGTH = PAIR_OFFSET + 8;

    private Side side; // side of the liquidity taker
    private long time;
    private double price;
    private double size;
    private Venue venue;
    private long receiveTime;
    private String pair;

    //pojo if needed; could convert to flyweight if needed later.
    public static Trade fromDirectBuffer(DirectBuffer source, int offset) {
        final Trade result = new Trade();
        result.setSide(source.getByte(offset + SIDE_OFFSET) == (byte) 0 ? Side.buy : Side.sell);
        result.setTime(source.getLong(offset + TIME_OFFSET));
        result.setPrice(source.getDouble(offset + PRICE_OFFSET));
        result.setSize(source.getDouble(offset + SIZE_OFFSET));
        result.setVenue(Venue.values[source.getInt(offset + VENUE_OFFSET)]);
        result.setReceiveTime(source.getLong(offset + RECEIVE_TIME_OFFSET));
        result.setPair(source.getStringWithoutLengthAscii(offset + PAIR_OFFSET, 8));
        return result;
    }

    public final void writeToByteBuffer(MutableDirectBuffer destination, int offset) {
        destination.putByte(offset + SIDE_OFFSET, side == Side.buy ? (byte) 0 : (byte) 1);
        destination.putLong(offset + TIME_OFFSET, time);
        destination.putDouble(offset + PRICE_OFFSET, price);
        destination.putDouble(offset + SIZE_OFFSET, size);
        destination.putInt(offset + VENUE_OFFSET, venue.ordinal());
        destination.putLong(offset + RECEIVE_TIME_OFFSET, receiveTime);
        destination.putStringWithoutLengthAscii(offset + PAIR_OFFSET, pair);
    }

}
