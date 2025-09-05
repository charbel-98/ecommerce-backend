package com.charbel.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddStockResponse {

	private int updatedVariantsCount;
	private List<VariantStockUpdateResult> results;
}