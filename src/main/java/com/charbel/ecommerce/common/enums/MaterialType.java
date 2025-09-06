package com.charbel.ecommerce.common.enums;

public enum MaterialType {
	COTTON("Cotton"), POLYESTER("Polyester"), WOOL("Wool"), SILK("Silk"), LEATHER("Leather"), DENIM("Denim"), LINEN(
			"Linen"), CASHMERE("Cashmere"), NYLON("Nylon"), SPANDEX("Spandex"), BLEND("Blend");

	private final String displayName;

	MaterialType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
