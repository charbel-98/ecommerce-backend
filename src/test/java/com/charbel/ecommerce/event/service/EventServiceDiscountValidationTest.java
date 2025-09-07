package com.charbel.ecommerce.event.service;

import com.charbel.ecommerce.event.entity.Discount;
import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.event.repository.DiscountRepository;
import com.charbel.ecommerce.event.repository.EventRepository;
import com.charbel.ecommerce.cdn.service.CdnService;
import com.charbel.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceDiscountValidationTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CdnService cdnService;

    @Mock
    private MultipartFile imageFile;

    @InjectMocks
    private EventService eventService;

    @Test
    void createEvent_WithDiscountConflict_ShouldThrowException() throws Exception {
        // Given
        Event event = Event.builder()
                .name("Test Event")
                .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 1, 31, 23, 59))
                .build();

        Discount discount = Discount.builder()
                .type(Discount.DiscountType.PERCENTAGE)
                .value(new BigDecimal("10.00"))
                .build();

        when(eventRepository.existsByName("Test Event")).thenReturn(false);
        when(eventRepository.existsActiveEventWithDiscountInDateRange(any(), any())).thenReturn(true);

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.createEvent(event, Arrays.asList(discount), imageFile)
        );

        assert(exception.getMessage().contains("Another active event with discount already exists"));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createEvent_WithoutDiscountConflict_ShouldSucceed() throws Exception {
        // Given
        Event event = Event.builder()
                .name("Test Event")
                .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 1, 31, 23, 59))
                .build();

        Discount discount = Discount.builder()
                .type(Discount.DiscountType.PERCENTAGE)
                .value(new BigDecimal("10.00"))
                .build();

        when(eventRepository.existsByName("Test Event")).thenReturn(false);
        when(eventRepository.existsActiveEventWithDiscountInDateRange(any(), any())).thenReturn(false);
        when(cdnService.uploadImage(any(), any())).thenReturn("http://example.com/image.jpg");
        when(eventRepository.save(any())).thenAnswer(invocation -> {
            Event savedEvent = invocation.getArgument(0);
            savedEvent.setId(UUID.randomUUID());
            return savedEvent;
        });

        // When/Then - Should not throw exception
        eventService.createEvent(event, Arrays.asList(discount), imageFile);

        verify(eventRepository).save(any());
        verify(discountRepository).saveAll(any());
    }

    @Test
    void updateEvent_WithDiscountConflictForNewDiscounts_ShouldThrowException() throws Exception {
        // Given
        UUID eventId = UUID.randomUUID();
        Event existingEvent = Event.builder()
                .id(eventId)
                .name("Existing Event")
                .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 1, 31, 23, 59))
                .build();

        Event updatedEvent = Event.builder()
                .name("Updated Event")
                .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 1, 31, 23, 59))
                .build();

        Discount discount = Discount.builder()
                .type(Discount.DiscountType.PERCENTAGE)
                .value(new BigDecimal("10.00"))
                .build();

        when(eventRepository.findByIdWithDiscounts(eventId)).thenReturn(java.util.Optional.of(existingEvent));
        when(eventRepository.existsActiveEventWithDiscountInDateRangeExcludingEvent(any(), any(), any())).thenReturn(true);

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.updateEvent(eventId, updatedEvent, Arrays.asList(discount), null)
        );

        assert(exception.getMessage().contains("Another active event with discount already exists"));
    }
}