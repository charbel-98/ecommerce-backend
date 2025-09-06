package com.charbel.ecommerce.event.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.charbel.ecommerce.event.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

	List<Event> findByStatusOrderByStartDateDesc(Event.EventStatus status);

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