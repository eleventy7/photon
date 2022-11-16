package com.skyscraper.event.binance;

import com.dslplatform.json.CompiledJson;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@CompiledJson
@EqualsAndHashCode(callSuper = true)
public class TradePayload extends Payload {
    public Trade data;
}