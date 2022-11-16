package com.skyscraper.event;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import lombok.Data;

@Data
@CompiledJson(formats = CompiledJson.Format.ARRAY)
public class ExchangeOrder {
    @JsonAttribute(index = 0)
    public double price;
    @JsonAttribute(index = 1)
    public double size;
}
