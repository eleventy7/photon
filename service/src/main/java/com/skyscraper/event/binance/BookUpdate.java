package com.skyscraper.event.binance;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.skyscraper.event.ExchangeOrder;
import lombok.Data;

@Data
@CompiledJson
public class BookUpdate {
    @JsonAttribute(name = "e", ignore = true)
    public String eventType;
    @JsonAttribute(name = "E")
    public long eventTime;
    @JsonAttribute(name = "s")
    public String pair;
    @JsonAttribute(name = "U")
    public long firstUpdateId;
    @JsonAttribute(name = "u")
    public long lastUpdateId;
    @JsonAttribute(name = "b")
    public ExchangeOrder[] bids;
    @JsonAttribute(name = "a")
    public ExchangeOrder[] asks;
}
