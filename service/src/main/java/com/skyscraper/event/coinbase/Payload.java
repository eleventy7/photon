package com.skyscraper.event.coinbase;

import com.dslplatform.json.CompiledJson;
import com.skyscraper.enums.CoinbaseType;
import lombok.Data;

@Data
@CompiledJson
public class Payload {
    public CoinbaseType type;
}
