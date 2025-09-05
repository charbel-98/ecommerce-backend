package com.charbel.ecommerce.common.enums;

public enum SizeType {
    XS("XS"),
    S("S"),
    M("M"),
    L("L"),
    XL("XL"),
    XXL("XXL"),
    XXXL("XXXL");

    private final String displayName;

    SizeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}