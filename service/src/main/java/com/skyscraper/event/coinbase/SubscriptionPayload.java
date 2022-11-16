package com.skyscraper.event.coinbase;

import com.dslplatform.json.CompiledJson;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@CompiledJson
@EqualsAndHashCode(callSuper = true)
public class SubscriptionPayload extends Payload {
    public CoinbaseSubscription[] channels;
}
