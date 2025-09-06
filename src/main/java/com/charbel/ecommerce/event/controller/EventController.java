package com.charbel.ecommerce.event.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.charbel.ecommerce.event.dto.AddProductsToEventRequest;
import com.charbel.ecommerce.event.dto.CreateEventRequest;
import com.charbel.ecommerce.event.dto.DiscountRequest;
import com.charbel.ecommerce.event.dto.DiscountResponse;
import com.charbel.ecommerce.event.dto.EventResponse;
import com.charbel.ecommerce.event.dto.UpdateEventRequest;
import com.charbel.ecommerce.event.entity.Discount;
import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.event.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Events", description = "Event management endpoints")
public class EventController {

	private final EventService eventService;

	@PostMapping("/admin/events")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a new event", description = "Creates a new event with image URL and optional discounts")
	public ResponseEntity<EventResponse> createEvent(@RequestBody @Valid CreateEventRequest request) {

		try {
			Event event = request.toEntity();
			List<Discount> discounts = null;

			if (request.getDiscounts() != null && !request.getDiscounts().isEmpty()) {
				discounts = request.getDiscounts().stream().map(DiscountRequest::toEntity).collect(Collectors.toList());
			}

			Event createdEvent = eventService.createEvent(event, discounts);
			EventResponse response = EventResponse.fromEntity(createdEvent);

			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (IllegalArgumentException e) {
			log.error("Validation error creating event: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Error creating event", e);
			throw new RuntimeException("Failed to create event");
		}
	}

	@PutMapping("/admin/events/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update an event", description = "Updates an existing event with optional image URL and discounts")
	public ResponseEntity<EventResponse> updateEvent(@PathVariable UUID id,
			@RequestBody @Valid UpdateEventRequest request) {

		try {
			Event event = request.toEntity();
			List<Discount> discounts = null;

			if (request.getDiscounts() != null) {
				discounts = request.getDiscounts().stream().map(DiscountRequest::toEntity).collect(Collectors.toList());
			}

			Event updatedEvent = eventService.updateEvent(id, event, discounts);
			EventResponse response = EventResponse.fromEntity(updatedEvent);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			log.error("Validation error updating event: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Error updating event with id: {}", id, e);
			throw new RuntimeException("Failed to update event");
		}
	}

	@DeleteMapping("/admin/events/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete an event", description = "Deletes an event and its associated data")
	public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
		try {
			eventService.deleteEvent(id);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			log.error("Error deleting event with id: {}", id, e);
			throw new RuntimeException("Failed to delete event");
		}
	}

	@GetMapping("/events/{id}")
	@Operation(summary = "Get event by ID", description = "Retrieves detailed event information including discounts")
	public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id) {
		Event event = eventService.getEventById(id);
		EventResponse response = EventResponse.fromEntityWithDiscounts(event);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/events")
	@Operation(summary = "Get all events", description = "Retrieves paginated list of all events")
	public ResponseEntity<Page<EventResponse>> getAllEvents(
			@RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
			@RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<Event> events = eventService.getAllEvents(pageable);
		Page<EventResponse> response = events.map(EventResponse::fromEntityBasic);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/events/active")
	@Operation(summary = "Get active events", description = "Retrieves list of active events")
	public ResponseEntity<List<EventResponse>> getActiveEvents() {

		List<Event> events = eventService.getActiveEvents();
		List<EventResponse> response = events.stream().map(EventResponse::fromEntityBasic).collect(Collectors.toList());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/events/running")
	@Operation(summary = "Get currently running events", description = "Retrieves events that are currently active and within their date range")
	public ResponseEntity<List<EventResponse>> getCurrentlyRunningEvents() {
		List<Event> events = eventService.getCurrentlyRunningEvents();
		List<EventResponse> response = events.stream().map(EventResponse::fromEntityBasic).collect(Collectors.toList());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/admin/events/{id}/products")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Add products to event", description = "Associates products with an event")
	public ResponseEntity<EventResponse> addProductsToEvent(@PathVariable UUID id,
			@RequestBody @Valid AddProductsToEventRequest request) {

		try {
			Event updatedEvent = eventService.addProductsToEvent(id, request.getProductIds());
			EventResponse response = EventResponse.fromEntity(updatedEvent);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error adding products to event with id: {}", id, e);
			throw new RuntimeException("Failed to add products to event");
		}
	}

	@DeleteMapping("/admin/events/{id}/products")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Remove products from event", description = "Removes product associations from an event")
	public ResponseEntity<EventResponse> removeProductsFromEvent(@PathVariable UUID id,
			@RequestBody @Valid AddProductsToEventRequest request) {

		try {
			Event updatedEvent = eventService.removeProductsFromEvent(id, request.getProductIds());
			EventResponse response = EventResponse.fromEntity(updatedEvent);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error removing products from event with id: {}", id, e);
			throw new RuntimeException("Failed to remove products from event");
		}
	}

	@GetMapping("/events/{id}/discounts")
	@Operation(summary = "Get event discounts", description = "Retrieves all discounts associated with an event")
	public ResponseEntity<List<DiscountResponse>> getEventDiscounts(@PathVariable UUID id) {
		List<Discount> discounts = eventService.getEventDiscounts(id);
		List<DiscountResponse> response = discounts.stream().map(DiscountResponse::fromEntity)
				.collect(Collectors.toList());
		return ResponseEntity.ok(response);
	}
}
