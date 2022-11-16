package com.skyscraper.event.coinbase;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.skyscraper.enums.Side;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@CompiledJson
@EqualsAndHashCode(callSuper = true)
public class Match extends Payload {
    @JsonAttribute(name = "product_id")
    public String pair;
    public OffsetDateTime time;
    public double size;
    public double price;
    @JsonAttribute(name = "side")
    public Side makerSide;
}
