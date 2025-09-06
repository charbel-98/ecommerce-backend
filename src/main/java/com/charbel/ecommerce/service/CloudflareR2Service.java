package com.charbel.ecommerce.service;

import com.charbel.ecommerce.config.CloudflareR2Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudflareR2Service {

	private final CloudflareR2Config r2Config;

	private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png",
			"image/webp", "image/gif");

	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	public String uploadEventImage(MultipartFile file) throws IOException {
		validateImageFile(file);

		S3Client s3Client = createS3Client();
		String filename = generateUniqueFilename(file.getOriginalFilename());
		String key = "events/" + filename;

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(r2Config.getBucketName()).key(key)
					.contentType(file.getContentType()).contentLength(file.getSize()).build();

			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			log.info("Successfully uploaded event image: {}", key);
			return r2Config.getCdnUrl(key);

		} catch (S3Exception e) {
			log.error("Failed to upload event image to R2: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to upload image to CDN", e);
		} finally {
			s3Client.close();
		}
	}

	public void deleteEventImage(String imageUrl) {
		if (imageUrl == null || !imageUrl.contains(r2Config.getCdnDomain())) {
			return;
		}

		S3Client s3Client = createS3Client();
		String key = extractKeyFromUrl(imageUrl);

		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(r2Config.getBucketName())
					.key(key).build();

			s3Client.deleteObject(deleteObjectRequest);
			log.info("Successfully deleted event image: {}", key);

		} catch (S3Exception e) {
			log.error("Failed to delete event image from R2: {}", e.getMessage(), e);
			// Don't throw exception for delete failures
		} finally {
			s3Client.close();
		}
	}

	public boolean imageExists(String imageUrl) {
		if (imageUrl == null || !imageUrl.contains(r2Config.getCdnDomain())) {
			return false;
		}

		S3Client s3Client = createS3Client();
		String key = extractKeyFromUrl(imageUrl);

		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(r2Config.getBucketName()).key(key)
					.build();

			s3Client.getObject(getObjectRequest);
			return true;

		} catch (S3Exception e) {
			return false;
		} finally {
			s3Client.close();
		}
	}

	private void validateImageFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Image file is required");
		}

		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
		}

		String contentType = file.getContentType();
		if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
			throw new IllegalArgumentException("Invalid image type. Allowed types: " + ALLOWED_IMAGE_TYPES);
		}
	}

	private S3Client createS3Client() {
		AwsBasicCredentials credentials = AwsBasicCredentials.create(r2Config.getAccessKeyId(),
				r2Config.getSecretAccessKey());

		return S3Client.builder().region(Region.US_EAST_1) // R2 uses auto region
				.endpointOverride(URI.create(r2Config.getEndpoint()))
				.credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
	}

	private String generateUniqueFilename(String originalFilename) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
		String uuid = UUID.randomUUID().toString().substring(0, 8);
		String extension = getFileExtension(originalFilename);
		return String.format("event-%s-%s%s", timestamp, uuid, extension);
	}

	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf("."));
	}

	private String extractKeyFromUrl(String imageUrl) {
		String cdnDomain = r2Config.getCdnDomain();
		int index = imageUrl.indexOf(cdnDomain) + cdnDomain.length() + 1;
		return imageUrl.substring(index);
	}
}