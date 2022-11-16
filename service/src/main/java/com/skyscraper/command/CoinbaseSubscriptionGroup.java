package com.skyscraper.command;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;
import com.skyscraper.enums.CoinbaseChannel;
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
public class CoinbaseSubscriptionGroup {
    public CoinbaseChannel name;
    @JsonAttribute(name = "product_ids")
    public List<String> productIds;
}
