package com.skyscraper.book;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EnrichedOrder
{
    private short side;
    private long time;
    private double sourcePrice;
    private double finalPrice;
    private double postFeePrice;
    private double size;
    private short venue;
    private long receiveTime;
    private long microReceiveTime;
}
