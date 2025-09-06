package com.charbel.ecommerce.event.dto;

import com.charbel.ecommerce.event.entity.Event;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateEventRequest {

	@NotBlank(message = "Event name is required")
	private String name;

	private String description;

	@NotNull(message = "Start date is required")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startDate;

	@NotNull(message = "End date is required")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endDate;

	@NotNull(message = "Event status is required")
	private Event.EventStatus status;

	@Valid
	private List<DiscountRequest> discounts;

	public Event toEntity() {
		return Event.builder().name(name).description(description).startDate(startDate).endDate(endDate)
				.status(status).build();
	}
}