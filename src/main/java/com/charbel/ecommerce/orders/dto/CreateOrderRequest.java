package com.charbel.ecommerce.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

	@NotNull(message = "Address ID is required")
	private UUID addressId;

	@NotEmpty(message = "Order items cannot be empty")
	@Valid
	private List<OrderItemRequest> items;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class OrderItemRequest {
		@NotNull(message = "Variant ID is required")
		private UUID variantId;

		@NotNull(message = "Quantity is required")
		private Integer quantity;
	}
}