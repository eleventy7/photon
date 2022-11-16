package com.skyscraper.command;

import com.dslplatform.json.CompiledJson;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@CompiledJson
public class FtxPing {
    public String op = "ping";
}
