package com.skyscraper.command;

import com.dslplatform.json.CompiledJson;
import com.skyscraper.enums.CoinbaseChannel;
import com.skyscraper.enums.CoinbaseRequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CompiledJson
public class CoinbaseRequest {
    public CoinbaseRequestType type;
    public List<CoinbaseSubscriptionGroup> channels;
}
