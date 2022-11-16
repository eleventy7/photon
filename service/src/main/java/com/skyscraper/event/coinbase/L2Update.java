package com.skyscraper.event.coinbase;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.skyscraper.event.ExchangeOrderChange;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@CompiledJson
@EqualsAndHashCode(callSuper = true)
public class L2Update extends Payload {
    @JsonAttribute(name = "product_id")
    public String pair;
    public OffsetDateTime time;
    public ExchangeOrderChange[] changes;
}
