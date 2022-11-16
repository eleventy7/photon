package com.skyscraper.event.ftx;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.skyscraper.enums.FtxChannel;
import com.skyscraper.enums.FtxType;
import lombok.Data;

@Data
@CompiledJson
public class Payload {
    public FtxChannel channel;
    @JsonAttribute(name = "market")
    public String pair;
    public FtxType type;
    public int code;
    public String message;
}
