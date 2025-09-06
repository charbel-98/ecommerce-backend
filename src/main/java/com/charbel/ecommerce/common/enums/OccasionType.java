package com.charbel.ecommerce.common.enums;

public enum OccasionType {
	CASUAL("Casual"), FORMAL("Formal"), BUSINESS("Business"), SPORT("Sport"), PARTY("Party"), BEACH("Beach"), OUTDOOR(
			"Outdoor");

	private final String displayName;

	OccasionType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
