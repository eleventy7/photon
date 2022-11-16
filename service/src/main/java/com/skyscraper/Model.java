package com.skyscraper;

import com.skyscraper.event.binance.BookUpdate;
import com.skyscraper.event.binance.Trade;
import lombok.Getter;

@Getter
public class Model {

    private void updateLasts(BookUpdate update) {
        //removed
    }

    private double getVolumeOrderImbalance(BookUpdate update) {
        //removed
        return 0.0;
    }

    private double getOrderImbalanceRatio() {
        //removed
        return 0.0;
    }

    private double getMidPriceBasis() {
        //removed
        return 0.0;
    }

    private double getSpread() {
        //removed
        return 0.0;
    }

    public void onBookTicker(BookUpdate update) {
        //removed
    }

    public void onTrade(Trade trade) {
        //removed
    }
}
