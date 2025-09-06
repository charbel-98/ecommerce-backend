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
public class VariantStockUpdateResult {

	private UUID variantId;
	private String sku;
	private Integer previousStock;
	private Integer newStock;
	private Integer stockAdded;
}
