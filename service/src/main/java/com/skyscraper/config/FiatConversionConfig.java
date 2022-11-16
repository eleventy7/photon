package com.skyscraper.config;

import com.skyscraper.enums.CcyPair;
import com.skyscraper.enums.Venue;

public final class FiatConversionConfig {
    private FiatConversionConfig() {
        //
    }

    public static boolean requiresConversion(CcyPair ccyPair) {
        return ccyPair.getBase().isStableCoin();
    }
}
