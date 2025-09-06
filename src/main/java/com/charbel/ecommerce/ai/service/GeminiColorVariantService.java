package com.charbel.ecommerce.ai.service;

import com.charbel.ecommerce.ai.dto.GeminiRequest;
import com.charbel.ecommerce.ai.dto.GeminiResponse;
import com.charbel.ecommerce.cdn.service.CdnService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiColorVariantService implements ColorVariantImageService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final CdnService cdnService;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, String> generateColorVariantImages(byte[] originalImageBytes, String productName, List<String> colorVariants) {
        log.info("Generating color variant images for product: {} with colors: {}", productName, colorVariants);
        
        try {
            // Build the enhanced prompt for Gemini 2.5 Flash
            String prompt = buildEnhancedPrompt(productName, colorVariants);
            
            // Call Gemini API
            GeminiResponse response = callGeminiAPI(originalImageBytes, prompt);
            
            // Process response and upload images
            Map<String, String> uploadedUrls = processGeminiResponse(response, productName);
            
            // If Gemini didn't return proper variants, create fallback images
            if (uploadedUrls.isEmpty()) {
                log.warn("No images returned from Gemini, creating fallback with original image");
                uploadedUrls = createFallbackImages(originalImageBytes, colorVariants);
            }
            
            log.info("Successfully generated {} variant images", uploadedUrls.size());
            return uploadedUrls;
            
        } catch (Exception e) {
            log.error("Failed to generate color variant images", e);
            // Fallback to original image for all variants
            return createFallbackImages(originalImageBytes, colorVariants);
        }
    }

    private String buildEnhancedPrompt(String productName, List<String> colorVariants) {
        return String.format(
                "Generate a new enhanced version of this image. Preserve the same person and pose. " +
                "Generate also %d more images keeping the person/pose identical, " +
                "only changing the %s color to match each of these variants: %s. " +
                "Each image must have a unique identifier: \"enhancedOriginal\" for the base enhancement, " +
                "and each color name (%s) as identifiers for the color variants. " +
                "Preserve texture, lighting, folds, and background in every image. " +
                "Return images in order: enhanced original first, then each color variant.",
                colorVariants.size(),
                productName,
                String.join(", ", colorVariants),
                String.join("\", \"", colorVariants)
        );
    }

    private GeminiResponse callGeminiAPI(byte[] originalImageBytes, String prompt) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = apiUrl + "?key=" + apiKey;
            HttpPost request = new HttpPost(url);
            
            // Create request body
            String base64Image = Base64.getEncoder().encodeToString(originalImageBytes);
            GeminiRequest geminiRequest = GeminiRequest.createImageRequest(prompt, base64Image);
            String jsonBody = objectMapper.writeValueAsString(geminiRequest);
            
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
            request.setHeader("Content-Type", "application/json");
            
            log.debug("Calling Gemini API for image generation");
            
            return httpClient.execute(request, response -> {
                if (response.getCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    String responseBody = new String(entity.getContent().readAllBytes());
                    log.debug("Gemini API response received");
                    return objectMapper.readValue(responseBody, GeminiResponse.class);
                } else {
                    log.error("Gemini API returned error code: {}", response.getCode());
                    throw new RuntimeException("Gemini API error: " + response.getCode());
                }
            });
        }
    }

    private Map<String, String> processGeminiResponse(GeminiResponse response, String productName) {
        Map<String, String> uploadedUrls = new HashMap<>();
        
        if (response == null || response.getCandidates() == null) {
            log.warn("Empty response from Gemini API");
            return uploadedUrls;
        }
        
        try {
            for (GeminiResponse.Candidate candidate : response.getCandidates()) {
                if (candidate.getContent() != null && candidate.getContent().getParts() != null) {
                    for (GeminiResponse.Part part : candidate.getContent().getParts()) {
                        if (part.getInline_data() != null) {
                            processImagePart(part, productName, uploadedUrls);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing Gemini response", e);
        }
        
        return uploadedUrls;
    }

    private void processImagePart(GeminiResponse.Part part, String productName, Map<String, String> uploadedUrls) {
        try {
            String base64Data = part.getInline_data().getData();
            String identifier = part.getInline_data().getIdentifier();
            
            // If no identifier provided, create one based on order
            if (identifier == null) {
                identifier = "variant-" + uploadedUrls.size();
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String fileName = String.format("%s-%s-%s.jpg", 
                productName.toLowerCase().replaceAll("\\s+", "-"), 
                identifier,
                UUID.randomUUID().toString().substring(0, 8));
            
            MultipartFile imageFile = createMultipartFile(imageBytes, fileName);
            String cdnUrl = cdnService.uploadImage(imageFile, "product-variants");
            
            uploadedUrls.put(identifier, cdnUrl);
            log.debug("Uploaded image for identifier: {} -> {}", identifier, cdnUrl);
            
        } catch (Exception e) {
            log.error("Error processing image part", e);
        }
    }

    private Map<String, String> createFallbackImages(byte[] originalImageBytes, List<String> colorVariants) {
        Map<String, String> fallbackUrls = new HashMap<>();
        
        try {
            // Upload original as enhanced original
            String enhancedFileName = String.format("enhanced-original-%s.jpg", 
                UUID.randomUUID().toString().substring(0, 8));
            MultipartFile enhancedFile = createMultipartFile(originalImageBytes, enhancedFileName);
            String enhancedUrl = cdnService.uploadImage(enhancedFile, "product-variants");
            fallbackUrls.put("enhancedOriginal", enhancedUrl);
            
            // Use original image for all color variants as fallback
            for (String color : colorVariants) {
                String variantFileName = String.format("variant-%s-%s.jpg", 
                    color.toLowerCase(),
                    UUID.randomUUID().toString().substring(0, 8));
                MultipartFile variantFile = createMultipartFile(originalImageBytes, variantFileName);
                String variantUrl = cdnService.uploadImage(variantFile, "product-variants");
                fallbackUrls.put(color, variantUrl);
            }
            
            log.info("Created fallback images for {} variants", colorVariants.size());
            
        } catch (Exception e) {
            log.error("Failed to create fallback images", e);
        }
        
        return fallbackUrls;
    }

    private MultipartFile createMultipartFile(byte[] content, String fileName) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "image/jpeg";
            }

            @Override
            public boolean isEmpty() {
                return content.length == 0;
            }

            @Override
            public long getSize() {
                return content.length;
            }

            @Override
            public byte[] getBytes() {
                return content;
            }

            @Override
            public java.io.InputStream getInputStream() {
                return new java.io.ByteArrayInputStream(content);
            }

            @Override
            public void transferTo(java.io.File dest) throws java.io.IOException {
                java.nio.file.Files.write(dest.toPath(), content);
            }
        };
    }
}