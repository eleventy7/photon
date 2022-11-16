package com.skyscraper.enums;

import lombok.Getter;

@Getter
public enum Venue {
    Coinbase((short) 0),
    Binance((short) 1),
    FTX((short) 2);

    public static final Venue[] values = values();
    private final short venueRepresentation;

    Venue(short venueRepresentation) {
        this.venueRepresentation = venueRepresentation;
    }

    public static Venue fromRepresentation(short representation) {
        if (representation == Coinbase.getRepresentation()) {
            return Coinbase;
        } else if (representation == Binance.getRepresentation()) {
            return Binance;
        } else if (representation == FTX.getRepresentation()) {
            return FTX;
        }
        return null;
    }

    public short getRepresentation() {
        return venueRepresentation;
    }
}
