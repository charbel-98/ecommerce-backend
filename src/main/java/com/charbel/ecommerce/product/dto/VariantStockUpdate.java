package com.charbel.ecommerce.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantStockUpdate {

	@NotNull(message = "Variant ID is required")
	private UUID variantId;

	@NotNull(message = "Stock quantity is required")
	@PositiveOrZero(message = "Stock quantity must be zero or positive")
	private Integer stockToAdd;
}