package com.charbel.ecommerce.common.enums;

public enum FitType {
    SLIM("Slim"),
    REGULAR("Regular"),
    LOOSE("Loose"),
    OVERSIZED("Oversized"),
    TAILORED("Tailored");

    private final String displayName;

    FitType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}