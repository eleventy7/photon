package com.skyscraper.event.ftx;

import com.dslplatform.json.CompiledJson;
import com.skyscraper.enums.Side;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@CompiledJson
public class Trade {
    public double price;
    public double size;
    public Side side;
    public boolean liquidation;
    public OffsetDateTime time;
}
