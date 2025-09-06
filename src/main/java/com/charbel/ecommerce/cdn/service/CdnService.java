package com.charbel.ecommerce.cdn.service;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Service
@Slf4j
public class CdnService {

	@Value("${r2.bucket.name}")
	private String bucketName;

	@Value("${r2.cdn.domain}")
	private String cdnDomain;

	@Value("${r2.account.id}")
	private String accountId;

	@Value("${r2.access.key.id}")
	private String accessKeyId;

	@Value("${r2.secret.access.key}")
	private String secretAccessKey;

	private S3Client getS3Client() {
		log.info("Creating S3 client with account ID: {}, bucket: {}, access key ID: {}", accountId, bucketName,
				accessKeyId);
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

		String endpoint = "https://" + accountId + ".r2.cloudflarestorage.com";
		log.info("Using R2 endpoint: {}", endpoint);

		return S3Client.builder().region(Region.of("auto")) // Cloudflare R2 uses "auto" region
				.endpointOverride(URI.create(endpoint)).credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.build();
	}

	public String uploadImage(MultipartFile file, String folder) throws IOException {
		log.info("Uploading image {} to folder {}", file.getOriginalFilename(), folder);

		// Generate unique filename
		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}

		String fileName = folder + "/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString() + extension;

		log.info("Generated filename: {}", fileName);

		try (S3Client s3Client = getS3Client()) {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(fileName)
					.contentType(file.getContentType()).contentLength(file.getSize()).build();

			PutObjectResponse response = s3Client.putObject(putObjectRequest,
					RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			log.info("Upload successful. ETag: {}", response.eTag());

			// Return CDN URL
			String cdnUrl = "https://" + cdnDomain + "/" + fileName;
			log.info("CDN URL: {}", cdnUrl);

			return cdnUrl;
		} catch (Exception e) {
			log.error("Failed to upload image to R2", e);
			throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
		}
	}

	public void deleteImageByUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			return;
		}

		try (S3Client s3Client = getS3Client()) {
			String key = extractKeyFromUrl(imageUrl);
			if (key == null || key.isBlank()) {
				log.warn("Could not extract CDN key from URL: {}", imageUrl);
				return;
			}

			log.info("Deleting image from CDN. Bucket: {}, Key: {}", bucketName, key);
			DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
			s3Client.deleteObject(deleteRequest);
			log.info("Image deleted successfully from CDN: {}", imageUrl);
		} catch (Exception e) {
			log.error("Failed to delete image from R2: {}", imageUrl, e);
		}
	}

	private String extractKeyFromUrl(String imageUrl) {
		try {
			URI uri = URI.create(imageUrl);
			String host = uri.getHost();
			String path = uri.getPath();
			if (path == null) {
				return null;
			}
			// If the URL already points to our CDN domain, strip the leading '/'
			if (host != null && host.equalsIgnoreCase(cdnDomain)) {
				return path.startsWith("/") ? path.substring(1) : path;
			}
			// Fallback: try to remove 'https://{cdnDomain}/' prefix if present in raw
			// string
			String prefix = "https://" + cdnDomain + "/";
			if (imageUrl.startsWith(prefix)) {
				return imageUrl.substring(prefix.length());
			}
			return path.startsWith("/") ? path.substring(1) : path;
		} catch (Exception e) {
			log.warn("Failed to parse CDN URL for key extraction: {}", imageUrl, e);
			return null;
		}
	}
}
