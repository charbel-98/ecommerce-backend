package com.charbel.ecommerce.event.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.charbel.ecommerce.event.entity.Event;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEventRequest {

	@NotBlank(message = "Event name is required")
	private String name;

	private String description;

	@NotNull(message = "Start date is required")
	@Future(message = "Start date must be in the future")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startDate;

	@NotNull(message = "End date is required")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endDate;

	private Event.EventStatus status = Event.EventStatus.SCHEDULED;

	@Valid
	private List<DiscountRequest> discounts;

	@NotNull(message = "Image file is required")
	private MultipartFile image;

	public Event toEntity(String imageUrl) {
		return Event.builder().name(name).description(description).startDate(startDate).endDate(endDate)
				.status(status != null ? status : Event.EventStatus.SCHEDULED).imageUrl(imageUrl).build();
	}
}
