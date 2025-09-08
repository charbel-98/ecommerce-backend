package com.charbel.ecommerce.orders.dto;

import com.charbel.ecommerce.product.dto.ProductImageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

	private UUID id;
	private UUID variantId;
	private String sku;
	private Map<String, Object> attributes;
	private String productName;
	private Integer quantity;
	private BigDecimal unitPrice;
	private BigDecimal totalPrice;
	private List<ProductImageResponse> images;
}
