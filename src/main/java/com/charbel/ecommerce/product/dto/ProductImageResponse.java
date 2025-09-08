package com.charbel.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {

	private UUID id;
	private String imageUrl;
	private String altText;
	private boolean isPrimary;
	private int sortOrder;
}