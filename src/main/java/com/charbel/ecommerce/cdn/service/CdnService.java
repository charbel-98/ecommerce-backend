package com.charbel.ecommerce.cdn.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

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
}
