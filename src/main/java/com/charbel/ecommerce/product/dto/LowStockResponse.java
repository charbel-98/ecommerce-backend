package com.charbel.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockResponse {
	
	private UUID variantId;
	private String sku;
	private Map<String, Object> attributes;
	private Integer stock;
	private UUID productId;
	private String productName;
	private String productDescription;
}