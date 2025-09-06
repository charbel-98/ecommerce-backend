package com.charbel.ecommerce.common.enums;

public enum SeasonType {
	SPRING("Spring"), SUMMER("Summer"), FALL("Fall"), WINTER("Winter"), ALL_SEASON("All Season");

	private final String displayName;

	SeasonType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
