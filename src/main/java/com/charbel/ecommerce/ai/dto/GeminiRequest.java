package com.charbel.ecommerce.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequest {
    private List<Content> contents;
    private GenerationConfig generationConfig;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
        private InlineData inline_data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InlineData {
        private String mime_type;
        private String data; // base64 encoded image
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {
        private Double temperature;
        private Integer maxOutputTokens;
    }

    public static GeminiRequest createImageRequest(String prompt, String base64Image) {
        InlineData inlineData = new InlineData("image/jpeg", base64Image);
        
        Part textPart = new Part(prompt, null);
        Part imagePart = new Part(null, inlineData);
        
        Content content = new Content(List.of(textPart, imagePart));
        GenerationConfig config = new GenerationConfig(0.7, 1024);
        
        return new GeminiRequest(List.of(content), config);
    }
}