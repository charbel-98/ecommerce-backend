package com.charbel.ecommerce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloudflare.r2")
@Data
public class CloudflareR2Config {

	private String bucketName;
	private String cdnDomain;
	private String accountId;
	private String accessKeyId;
	private String secretAccessKey;

	public String getEndpoint() {
		return String.format("https://%s.r2.cloudflarestorage.com", accountId);
	}

	public String getCdnUrl(String filename) {
		return String.format("https://%s/%s", cdnDomain, filename);
	}
}