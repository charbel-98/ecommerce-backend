package com.charbel.ecommerce.cdn.controller;

import com.charbel.ecommerce.cdn.service.CdnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CDN", description = "Image upload endpoints")
public class CdnController {

	private final CdnService cdnService;

	@PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Upload image to CDN", description = "Uploads an image file to Cloudflare R2 CDN. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Map<String, String>> uploadImage(
			@Parameter(description = "Image file to upload", required = true) @RequestParam("image") MultipartFile image,

			@Parameter(description = "Folder to upload to (e.g., 'products', 'variants')", required = true) @RequestParam("folder") String folder) {

		try {
			log.info("Received image upload request for folder: {}", folder);

			// Validate file
			if (image.isEmpty()) {
				log.warn("Empty file received");
				return ResponseEntity.badRequest().build();
			}

			// Validate content type
			String contentType = image.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				log.warn("Invalid file type: {}", contentType);
				Map<String, String> error = new HashMap<>();
				error.put("error", "Only image files are allowed");
				return ResponseEntity.badRequest().body(error);
			}

			// Validate file size (max 10MB)
			if (image.getSize() > 10 * 1024 * 1024) {
				log.warn("File too large: {} bytes", image.getSize());
				Map<String, String> error = new HashMap<>();
				error.put("error", "File size must be less than 10MB");
				return ResponseEntity.badRequest().body(error);
			}

			// Clean folder name
			String cleanFolder = folder.replaceAll("[^a-zA-Z0-9-_]", "").toLowerCase();
			if (cleanFolder.isEmpty()) {
				cleanFolder = "general";
			}

			log.info("Uploading file: {} ({} bytes) to folder: {}", image.getOriginalFilename(), image.getSize(),
					cleanFolder);

			String cdnUrl = cdnService.uploadImage(image, cleanFolder);

			Map<String, String> response = new HashMap<>();
			response.put("url", cdnUrl);

			log.info("Image uploaded successfully: {}", cdnUrl);
			return ResponseEntity.ok(response);

		} catch (IOException e) {
			log.error("Failed to upload image", e);
			Map<String, String> error = new HashMap<>();
			error.put("error", "Failed to upload image: " + e.getMessage());
			return ResponseEntity.internalServerError().body(error);
		} catch (Exception e) {
			log.error("Unexpected error during image upload", e);
			Map<String, String> error = new HashMap<>();
			error.put("error", "Unexpected error occurred");
			return ResponseEntity.internalServerError().body(error);
		}
	}
}
