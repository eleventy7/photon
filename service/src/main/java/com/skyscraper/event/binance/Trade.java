package com.skyscraper.event.binance;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import lombok.Data;

@Data
@CompiledJson
public class Trade {
    @JsonAttribute(name = "e", ignore = true)
    public String event;
    @JsonAttribute(name = "E")
    public long eventTime;
    @JsonAttribute(name = "s")
    public String pair;
    @JsonAttribute(name = "t", ignore = true)
    public long tradeId;
    @JsonAttribute(name = "p")
    public double price;
    @JsonAttribute(name = "q")
    public double size;
    @JsonAttribute(name = "b", ignore = true)
    public long buyOrderId;
    @JsonAttribute(name = "a", ignore = true)
    public long askOrderId;
    @JsonAttribute(name = "T")
    public long tradeTime;
    @JsonAttribute(name = "m", ignore = true)
    public boolean buyerMarketMaker;
    @JsonAttribute(name = "M", ignore = true)
    public boolean ignore;
}
