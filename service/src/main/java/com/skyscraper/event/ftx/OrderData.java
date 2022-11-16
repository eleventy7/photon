package com.skyscraper.event.ftx;

import com.dslplatform.json.CompiledJson;
import com.skyscraper.enums.FtxAction;
import com.skyscraper.event.ExchangeOrder;
import lombok.Data;

@Data
@CompiledJson
public class OrderData {
    public FtxAction ftxAction;
    public ExchangeOrder[] bids;
    public ExchangeOrder[] asks;
    public long checksum;
    public double time; // whole number part == seconds

    public long getEpoch() {
        return (long) time * 1000;
    }
}
