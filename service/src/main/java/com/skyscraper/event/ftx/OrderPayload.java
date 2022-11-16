package com.skyscraper.event.ftx;

import com.dslplatform.json.CompiledJson;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@CompiledJson
@EqualsAndHashCode(callSuper = true)
public class OrderPayload extends Payload {
    public OrderData data;
}