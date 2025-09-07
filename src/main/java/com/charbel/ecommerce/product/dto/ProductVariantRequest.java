package com.charbel.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductVariantRequest {

	@NotBlank(message = "SKU is required")
	private String sku;

	@NotNull(message = "Attributes are required")
	private Map<String, Object> attributes;

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", message = "Price must be zero or positive")
	private BigDecimal price;

	@NotNull(message = "Stock is required")
	@PositiveOrZero(message = "Stock must be zero or positive")
	private Integer stock;
}
