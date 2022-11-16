package com.skyscraper.event.binance;

import com.dslplatform.json.CompiledJson;
import lombok.Data;

@Data
@CompiledJson
public class Payload {
    public String stream;
}
