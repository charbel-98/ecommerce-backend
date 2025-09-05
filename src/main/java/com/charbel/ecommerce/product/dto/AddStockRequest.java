package com.charbel.ecommerce.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddStockRequest {

	@NotEmpty(message = "Stock updates list cannot be empty")
	@Valid
	private List<VariantStockUpdate> stockUpdates;
}