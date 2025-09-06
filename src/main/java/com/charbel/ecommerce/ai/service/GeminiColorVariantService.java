package com.charbel.ecommerce.ai.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.charbel.ecommerce.ai.dto.GeminiResponse;
import com.charbel.ecommerce.cdn.service.CdnService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	public Map<String, String> generateColorVariantImages(byte[] originalImageBytes, String productName,
			List<String> colorVariants) {
		log.info("=== STARTING COLOR VARIANT GENERATION ===");
		log.info("Product name: {}", productName);
		log.info("Color variants requested: {}", colorVariants);
		log.info("Original image size: {} bytes", originalImageBytes.length);

		Map<String, String> generatedUrls = new HashMap<>();

		try {
			// First, generate an enhanced version of the original
			log.info("Generating enhanced original image...");
			String enhancedPrompt = buildEnhancedOriginalPrompt(productName, originalImageBytes);
			log.info("Enhanced prompt: {}", enhancedPrompt);
			GeminiResponse enhancedResponse = callGeminiImageAPI(enhancedPrompt, originalImageBytes);
			String enhancedUrl = processSingleImageResponse(enhancedResponse, productName, "enhancedOriginal");

			if (enhancedUrl != null) {
				generatedUrls.put("enhancedOriginal", enhancedUrl);
				log.info("Successfully generated enhanced original: {}", enhancedUrl);
			}

			// Generate each color variant individually
			for (String color : colorVariants) {
				log.info("Generating variant for color: {}", color);
				try {
					String variantPrompt = buildColorVariantPrompt(productName, color, originalImageBytes);
					log.info("Variant prompt: {}", variantPrompt);
					GeminiResponse variantResponse = callGeminiImageAPI(variantPrompt, originalImageBytes);
					String variantUrl = processSingleImageResponse(variantResponse, productName, color);

					if (variantUrl != null) {
						generatedUrls.put(color, variantUrl);
						log.info("Successfully generated {} variant: {}", color, variantUrl);
					} else {
						log.warn("Failed to generate {} variant, will use fallback", color);
					}

					// Add small delay between requests to avoid rate limiting
					Thread.sleep(1000);

				} catch (Exception e) {
					log.error("Error generating {} variant: {}", color, e.getMessage());
				}
			}

			// If we didn't get enough images, create fallbacks for missing ones
			if (generatedUrls.size() < colorVariants.size() + 1) { // +1 for enhanced original
				log.warn("=== PARTIAL GENERATION - Creating fallbacks for missing variants ===");
				Map<String, String> fallbackUrls = createSelectiveFallbackImages(originalImageBytes, colorVariants,
						generatedUrls);
				generatedUrls.putAll(fallbackUrls);
			}

			log.info("=== GENERATION COMPLETED ===");
			log.info("Total images generated: {}", generatedUrls.size());
			log.info("Generated image keys: {}", generatedUrls.keySet());
			return generatedUrls;

		} catch (Exception e) {
			log.error("=== EXCEPTION TRIGGERED FALLBACK ===");
			log.error("Exception details: {}", e.getMessage(), e);
			log.warn("Using fallback images for all {} variants due to exception", colorVariants.size());
			return createFallbackImages(originalImageBytes, colorVariants);
		}
	}

	private String buildEnhancedOriginalPrompt(String productName, byte[] originalImageBytes) {
		return String.format("Enhance the quality of this image while keeping the same product and person.");
	}

	private String buildColorVariantPrompt(String productName, String color, byte[] originalImageBytes) {
		return String.format(
				"Create an identical copy of this image with ONLY the %s color changed to %s. "
						+ "Keep EVERYTHING else exactly the same: "
						+ "- Same person (same face, same skin tone, same hair, same body) "
						+ "- Same exact pose and body position " + "- Same background (every detail, shadow, texture) "
						+ "- Same lighting (direction, intensity, shadows) " + "- Same camera angle and composition "
						+ "- Same %s fit, wrinkles, and fabric texture " + "- Same image quality and sharpness "
						+ "ONLY change the %s from its current color to %s color. "
						+ "The %s color should look natural and realistic on the fabric. "
						+ "Everything else must remain pixel-perfect identical to the original image.",
				productName, color, productName, productName, color, color);
	}

	private GeminiResponse callGeminiImageAPI(String prompt, byte[] imageBytes) throws Exception {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			log.info("Gemini Image API URL: {}", apiUrl);

			HttpPost request = new HttpPost(apiUrl);

			// Use header-based authentication instead of query parameter
			request.setHeader("x-goog-api-key", apiKey);
			request.setHeader("Content-Type", "application/json");

			// Encode image to base64
			String base64Image = Base64.getEncoder().encodeToString(imageBytes);

			// Create request body with both text prompt and image
			String jsonBody = String.format("""
					{
					    "contents": [{
					        "parts": [
					            {"text": "%s"},
					            {
					                "inlineData": {
					                    "mimeType": "image/jpeg",
					                    "data": "%s"
					                }
					            }
					        ]
					    }]
					}
					""", prompt.replace("\"", "\\\""), base64Image);

			log.info("Request payload size: {} characters", jsonBody.length());
			log.debug("Request payload: {}", jsonBody);

			request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

			log.info("Sending request to Gemini Image API...");

			return httpClient.execute(request, response -> {
				int statusCode = response.getCode();
				log.info("Gemini API response status: {}", statusCode);

				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					String responseBody = new String(entity.getContent().readAllBytes());
					log.info("Gemini API response body length: {} characters", responseBody.length());
					log.debug("Gemini API response body: {}", responseBody);

					GeminiResponse geminiResponse = objectMapper.readValue(responseBody, GeminiResponse.class);
					log.info("Parsed Gemini response successfully");
					return geminiResponse;
				} else {
					HttpEntity entity = response.getEntity();
					String errorBody = entity != null
							? new String(entity.getContent().readAllBytes())
							: "No error body";
					log.error("Gemini API error - Status: {}, Body: {}", statusCode, errorBody);
					throw new RuntimeException("Gemini API error: " + statusCode + " - " + errorBody);
				}
			});
		}
	}

	private String processSingleImageResponse(GeminiResponse response, String productName, String identifier) {
		log.info("=== PROCESSING SINGLE IMAGE RESPONSE for {} ===", identifier);

		if (response == null) {
			log.warn("Response is null");
			return null;
		}

		if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
			log.warn("Response candidates is null or empty");
			return null;
		}

		try {
			GeminiResponse.Candidate candidate = response.getCandidates().get(0);
			log.info("Processing first candidate for {}", identifier);
			log.info("Candidate finish reason: {}", candidate.getFinishReason());

			if (candidate.getContent() == null) {
				log.warn("Candidate content is null");
				return null;
			}

			if (candidate.getContent().getParts() == null || candidate.getContent().getParts().isEmpty()) {
				log.warn("Candidate parts is null or empty");
				return null;
			}

			log.info("Found {} parts in candidate", candidate.getContent().getParts().size());

			for (int i = 0; i < candidate.getContent().getParts().size(); i++) {
				GeminiResponse.Part part = candidate.getContent().getParts().get(i);
				log.info("Processing part {} for {}", i, identifier);

				// Check for inline image data first
				if (part.getInline_data() != null && part.getInline_data().getData() != null) {
					log.info("Found inline image data for {}, length: {}", identifier,
							part.getInline_data().getData().length());
					log.info("MIME type: {}", part.getInline_data().getMime_type());
					return uploadImageFromBase64(part.getInline_data().getData(), productName, identifier);
				}

				if (part.getText() != null) {
					log.info("Part {} contains text for {}: {}", i, identifier,
							part.getText().substring(0, Math.min(200, part.getText().length())));
				}
			}
		} catch (Exception e) {
			log.error("Error processing single image response for {}: {}", identifier, e.getMessage(), e);
		}

		log.warn("No valid image data found in response for {}", identifier);
		return null;
	}

	private String uploadImageFromBase64(String base64Data, String productName, String identifier) {
		try {
			log.info("Uploading image for identifier: {}", identifier);

			if (base64Data == null || base64Data.trim().isEmpty()) {
				log.error("Base64 data is null or empty for identifier: {}", identifier);
				return null;
			}

			byte[] imageBytes = Base64.getDecoder().decode(base64Data);
			log.info("Successfully decoded {} bytes of image data for identifier: {}", imageBytes.length, identifier);

			String fileName = String.format("%s-%s-%s.jpg", productName.toLowerCase().replaceAll("\\s+", "-"),
					identifier.toLowerCase(), UUID.randomUUID().toString().substring(0, 8));

			log.info("Generated filename: {}", fileName);

			MultipartFile imageFile = createMultipartFile(imageBytes, fileName);
			String cdnUrl = cdnService.uploadImage(imageFile, "product-variants");

			log.info("Successfully uploaded image - Identifier: {} -> URL: {}", identifier, cdnUrl);
			return cdnUrl;

		} catch (Exception e) {
			log.error("Error uploading image for identifier {}: {}", identifier, e.getMessage());
			return null;
		}
	}

	private Map<String, String> createSelectiveFallbackImages(byte[] originalImageBytes, List<String> colorVariants,
			Map<String, String> existingUrls) {
		Map<String, String> fallbackUrls = new HashMap<>();

		log.info("=== CREATING SELECTIVE FALLBACK IMAGES ===");
		log.info("Missing variants that need fallbacks");

		try {
			// Create enhanced original if missing
			if (!existingUrls.containsKey("enhancedOriginal")) {
				String enhancedFileName = String.format("fallback-enhanced-original-%s.jpg",
						UUID.randomUUID().toString().substring(0, 8));
				MultipartFile enhancedFile = createMultipartFile(originalImageBytes, enhancedFileName);
				String enhancedUrl = cdnService.uploadImage(enhancedFile, "product-variants");
				fallbackUrls.put("enhancedOriginal", enhancedUrl);
				log.info("Created fallback enhancedOriginal: {}", enhancedUrl);
			}

			// Create fallback for missing color variants
			for (String color : colorVariants) {
				if (!existingUrls.containsKey(color)) {
					String variantFileName = String.format("fallback-variant-%s-%s.jpg", color.toLowerCase(),
							UUID.randomUUID().toString().substring(0, 8));
					MultipartFile variantFile = createMultipartFile(originalImageBytes, variantFileName);
					String variantUrl = cdnService.uploadImage(variantFile, "product-variants");
					fallbackUrls.put(color, variantUrl);
					log.info("Created fallback for color '{}': {}", color, variantUrl);
				}
			}

			log.info("=== SELECTIVE FALLBACK CREATION COMPLETED ===");
			log.info("Fallback images created: {}", fallbackUrls.size());

		} catch (Exception e) {
			log.error("Failed to create selective fallback images", e);
		}

		return fallbackUrls;
	}

	private Map<String, String> createFallbackImages(byte[] originalImageBytes, List<String> colorVariants) {
		Map<String, String> fallbackUrls = new HashMap<>();

		log.info("=== CREATING FALLBACK IMAGES ===");
		log.info("Original image size: {} bytes", originalImageBytes.length);
		log.info("Creating fallback for variants: {}", colorVariants);

		try {
			// Upload original as enhanced original
			String enhancedFileName = String.format("enhanced-original-%s.jpg",
					UUID.randomUUID().toString().substring(0, 8));
			MultipartFile enhancedFile = createMultipartFile(originalImageBytes, enhancedFileName);
			String enhancedUrl = cdnService.uploadImage(enhancedFile, "product-variants");
			fallbackUrls.put("enhancedOriginal", enhancedUrl);
			log.info("Created fallback enhancedOriginal: {}", enhancedUrl);

			// Use original image for all color variants as fallback
			for (String color : colorVariants) {
				String variantFileName = String.format("variant-%s-%s.jpg", color.toLowerCase(),
						UUID.randomUUID().toString().substring(0, 8));
				MultipartFile variantFile = createMultipartFile(originalImageBytes, variantFileName);
				String variantUrl = cdnService.uploadImage(variantFile, "product-variants");
				fallbackUrls.put(color, variantUrl);
				log.info("Created fallback for color '{}': {}", color, variantUrl);
			}

			log.info("=== FALLBACK CREATION COMPLETED ===");
			log.info("Total fallback images created: {}", fallbackUrls.size());
			log.info("Fallback image keys: {}", fallbackUrls.keySet());

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
