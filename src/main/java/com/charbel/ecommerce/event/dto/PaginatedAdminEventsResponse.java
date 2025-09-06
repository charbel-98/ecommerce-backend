package com.charbel.ecommerce.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedAdminEventsResponse {

	private List<AdminEventResponse> events;
	private int currentPage;
	private int totalPages;
	private long totalElements;
	private int pageSize;
	private boolean hasNext;
	private boolean hasPrevious;
}
