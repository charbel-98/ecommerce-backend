package com.charbel.ecommerce.common.enums;

public enum GenderType {
    MEN("Men"),
    WOMEN("Women"),
    KIDS("Kids"),
    UNISEX("Unisex");

    private final String displayName;

    GenderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}