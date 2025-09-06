package com.charbel.ecommerce.event.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class AddProductsToEventRequest {

	@NotEmpty(message = "Product IDs cannot be empty")
	private Set<UUID> productIds;
}
