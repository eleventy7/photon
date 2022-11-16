package com.skyscraper.command;

import com.dslplatform.json.CompiledJson;
import com.skyscraper.enums.FtxChannel;
import com.skyscraper.enums.FtxOp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CompiledJson
public class FtxRequest {
    public FtxOp op;
    public FtxChannel channel;
    public String market;
}
