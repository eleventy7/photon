package com.skyscraper.event.coinbase;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.skyscraper.event.ExchangeOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@CompiledJson
@EqualsAndHashCode(callSuper = true)
public class L2Snapshot extends Payload {
    @JsonAttribute(name = "product_id")
    public String pair;
    public ExchangeOrder[] bids;
    public ExchangeOrder[] asks;
}
