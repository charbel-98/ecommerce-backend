package com.charbel.ecommerce.event.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.charbel.ecommerce.cdn.service.CdnService;
import com.charbel.ecommerce.event.entity.Discount;
import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.event.repository.DiscountRepository;
import com.charbel.ecommerce.event.repository.EventRepository;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

	private final EventRepository eventRepository;
	private final DiscountRepository discountRepository;
	private final ProductRepository productRepository;
	private final CdnService cdnService;

	@Transactional
	public Event createEvent(Event event, List<Discount> discounts, MultipartFile imageFile) throws IOException {
		// Validate event name uniqueness
		if (eventRepository.existsByName(event.getName())) {
			throw new IllegalArgumentException("Event with name '" + event.getName() + "' already exists");
		}

		// Upload image to CDN
		String imageUrl = cdnService.uploadImage(imageFile, "events");
		event.setImageUrl(imageUrl);

		// Save event first
		Event savedEvent = eventRepository.save(event);

		// Create and save discounts
		if (discounts != null && !discounts.isEmpty()) {
			discounts.forEach(discount -> {
				discount.setEventId(savedEvent.getId());
				discount.setEvent(savedEvent);
			});
			discountRepository.saveAll(discounts);
			savedEvent.setDiscounts(discounts);
		}

		log.info("Created event: {} with ID: {}", savedEvent.getName(), savedEvent.getId());
		return savedEvent;
	}

	@Transactional
	public Event updateEvent(UUID eventId, Event updatedEvent, List<Discount> discounts, MultipartFile imageFile) throws IOException {
		Event existingEvent = eventRepository.findById(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

		// Check name uniqueness if name is being changed
		if (!existingEvent.getName().equals(updatedEvent.getName())
				&& eventRepository.existsByName(updatedEvent.getName())) {
			throw new IllegalArgumentException("Event with name '" + updatedEvent.getName() + "' already exists");
		}

		// Update basic fields
		existingEvent.setName(updatedEvent.getName());
		existingEvent.setDescription(updatedEvent.getDescription());
		existingEvent.setStartDate(updatedEvent.getStartDate());
		existingEvent.setEndDate(updatedEvent.getEndDate());
		existingEvent.setStatus(updatedEvent.getStatus());

		// Handle image update if new image is provided
		if (imageFile != null && !imageFile.isEmpty()) {
			// Delete old image from CDN
			String oldImageUrl = existingEvent.getImageUrl();
			if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
				cdnService.deleteImageByUrl(oldImageUrl);
			}
			
			// Upload new image to CDN
			String newImageUrl = cdnService.uploadImage(imageFile, "events");
			existingEvent.setImageUrl(newImageUrl);
		}

		// Update discounts
		if (discounts != null) {
			// Delete existing discounts
			discountRepository.deleteByEventId(eventId);
			// Create new discounts
			if (!discounts.isEmpty()) {
				discounts.forEach(discount -> {
					discount.setEventId(eventId);
					discount.setEvent(existingEvent);
				});
				discountRepository.saveAll(discounts);
				existingEvent.setDiscounts(discounts);
			}
		}

		Event savedEvent = eventRepository.save(existingEvent);
		log.info("Updated event: {} with ID: {}", savedEvent.getName(), savedEvent.getId());
		return savedEvent;
	}

	@Transactional
	public void deleteEvent(UUID eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

		// Delete image from CDN
		String imageUrl = event.getImageUrl();
		if (imageUrl != null && !imageUrl.trim().isEmpty()) {
			cdnService.deleteImageByUrl(imageUrl);
		}

		// Delete discounts (cascade will handle this, but explicit for clarity)
		discountRepository.deleteByEventId(eventId);

		// Delete event
		eventRepository.delete(event);
		log.info("Deleted event: {} with ID: {}", event.getName(), eventId);
	}

	public Event getEventById(UUID eventId) {
		return eventRepository.findByIdWithDiscountsAndProducts(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
	}

	public Page<Event> getAllEvents(Pageable pageable) {
		return eventRepository.findAll(pageable);
	}

	public Event getEventByIdForAdmin(UUID eventId) {
		return eventRepository.findByIdWithDiscountsAndProducts(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
	}

	public Page<Event> getAllEventsForAdmin(Pageable pageable) {
		return eventRepository.findAllWithDiscountsAndProducts(pageable);
	}

	public List<Event> getActiveEvents() {
		return eventRepository.findByStatusOrderByStartDateDesc(Event.EventStatus.ACTIVE);
	}

	public List<Event> getCurrentlyRunningEvents() {
		LocalDateTime now = LocalDateTime.now();
		return eventRepository.findActiveEvents(Event.EventStatus.ACTIVE, now);
	}

	@Transactional
	public Event addProductsToEvent(UUID eventId, Set<UUID> productIds) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

		List<Product> products = productRepository.findAllById(productIds);
		if (products.size() != productIds.size()) {
			throw new IllegalArgumentException("Some products not found");
		}

		event.getProducts().addAll(products);
		Event savedEvent = eventRepository.save(event);
		log.info("Added {} products to event: {}", products.size(), event.getName());
		return savedEvent;
	}

	@Transactional
	public Event removeProductsFromEvent(UUID eventId, Set<UUID> productIds) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

		List<Product> products = productRepository.findAllById(productIds);
		event.getProducts().removeAll(products);
		Event savedEvent = eventRepository.save(event);
		log.info("Removed {} products from event: {}", products.size(), event.getName());
		return savedEvent;
	}

	public List<Discount> getEventDiscounts(UUID eventId) {
		if (!eventRepository.existsById(eventId)) {
			throw new EntityNotFoundException("Event not found with id: " + eventId);
		}
		return discountRepository.findByEventId(eventId);
	}
}
