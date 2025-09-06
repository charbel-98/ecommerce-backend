package com.charbel.ecommerce.common.enums;

public enum ColorFamily {
	BLACK("Black", "#000000"), WHITE("White", "#FFFFFF"), GRAY("Gray", "#808080"), BROWN("Brown", "#964B00"), BEIGE(
			"Beige", "#F5F5DC"), RED("Red", "#FF0000"), PINK("Pink", "#FFC0CB"), ORANGE("Orange", "#FFA500"), YELLOW(
					"Yellow", "#FFFF00"), GREEN("Green", "#008000"), BLUE("Blue", "#0000FF"), PURPLE("Purple",
							"#800080"), NAVY("Navy", "#000080"), MULTICOLOR("Multicolor", "#FFFFFF");

	private final String displayName;
	private final String hexCode;

	ColorFamily(String displayName, String hexCode) {
		this.displayName = displayName;
		this.hexCode = hexCode;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getHexCode() {
		return hexCode;
	}
}
