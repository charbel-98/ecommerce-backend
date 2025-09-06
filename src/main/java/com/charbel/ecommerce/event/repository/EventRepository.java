package com.charbel.ecommerce.event.repository;

import com.charbel.ecommerce.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

	Page<Event> findByStatusOrderByStartDateDesc(Event.EventStatus status, Pageable pageable);

	List<Event> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Event.EventStatus status,
			LocalDateTime startDate, LocalDateTime endDate);

	@Query("SELECT e FROM Event e WHERE e.status = :status AND :now BETWEEN e.startDate AND e.endDate")
	List<Event> findActiveEvents(@Param("status") Event.EventStatus status, @Param("now") LocalDateTime now);

	@Query("SELECT e FROM Event e LEFT JOIN FETCH e.discounts LEFT JOIN FETCH e.products WHERE e.id = :id")
	Optional<Event> findByIdWithDiscountsAndProducts(@Param("id") UUID id);

	@Query("SELECT e FROM Event e LEFT JOIN FETCH e.discounts WHERE e.id = :id")
	Optional<Event> findByIdWithDiscounts(@Param("id") UUID id);

	boolean existsByName(String name);
}