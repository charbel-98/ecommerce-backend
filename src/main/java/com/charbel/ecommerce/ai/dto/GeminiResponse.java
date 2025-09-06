package com.charbel.ecommerce.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GeminiResponse {
	private List<Candidate> candidates;
	private PromptFeedback promptFeedback;

	@Data
	@NoArgsConstructor
	public static class Candidate {
		private Content content;
		private String finishReason;
		private Integer index;
		private List<SafetyRating> safetyRatings;
	}

	@Data
	@NoArgsConstructor
	public static class Content {
		private List<Part> parts;
		private String role;
	}

	@Data
	@NoArgsConstructor
	public static class Part {
		private String text;
		@JsonProperty("inlineData")
		private InlineData inline_data;
	}

	@Data
	@NoArgsConstructor
	public static class InlineData {
		@JsonProperty("mimeType")
		private String mime_type;
		private String data; // base64 encoded image
		private String identifier; // Custom identifier for variant tracking
	}

	@Data
	@NoArgsConstructor
	public static class SafetyRating {
		private String category;
		private String probability;
	}

	@Data
	@NoArgsConstructor
	public static class PromptFeedback {
		private List<SafetyRating> safetyRatings;
	}
}
