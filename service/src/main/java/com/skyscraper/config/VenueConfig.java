package com.skyscraper.config;

import com.skyscraper.enums.CcyPair;
import com.skyscraper.enums.Venue;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agrona.collections.Object2ObjectHashMap;

import java.util.Map;

@Data
@NoArgsConstructor
public class VenueConfig {
    private Venue venue;
    private String url;
    private String apiKey;
    private String secretKey;
    private Map<CcyPair, String> ccyPairToString;
    private Object2ObjectHashMap<String, CcyPair> stringToCcyPair = new Object2ObjectHashMap<>();
    private Fees fees;

    public void updateMaps() {
        ccyPairToString.forEach((asset, pair) -> stringToCcyPair.put(pair, asset));
    }
}
