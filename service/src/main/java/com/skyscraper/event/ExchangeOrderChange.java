package com.skyscraper.event;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.skyscraper.enums.Side;
import lombok.Data;

@Data
@CompiledJson(formats = CompiledJson.Format.ARRAY)
public class ExchangeOrderChange {
    @JsonAttribute(index = 0)
    public Side side;
    @JsonAttribute(index = 1)
    public double price;
    @JsonAttribute(index = 2)
    public double size;
}
