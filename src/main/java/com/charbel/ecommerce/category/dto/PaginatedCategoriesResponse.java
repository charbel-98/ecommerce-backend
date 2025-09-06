package com.charbel.ecommerce.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedCategoriesResponse {

	private List<CategoryWithProductsResponse> categories;
	private int currentPage;
	private int totalPages;
	private long totalElements;
	private int pageSize;
	private boolean hasNext;
	private boolean hasPrevious;
}