package com.skyscraper.enums;

public enum Side {
    buy((short)0),
    sell((short)1);

    private final short representation;

    public Side getOpposite() {
        if (this == buy) {
            return sell;
        }
        return buy;
    }

    Side(short representation) {
        this.representation = representation;
    }

    public short getRepresentation() {
        return representation;
    }

    public static Side fromRepresentation(short representation) {
        if (representation == buy.getRepresentation()) {
            return buy;
        } else if (representation == sell.getRepresentation()) {
            return sell;
        }
        return null;
    }
}
