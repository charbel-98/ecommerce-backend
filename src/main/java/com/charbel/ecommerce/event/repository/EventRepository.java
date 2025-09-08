package com.charbel.ecommerce.event.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.charbel.ecommerce.event.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

	@Query("SELECT e FROM Event e WHERE e.isDeleted = false AND e.status = :status ORDER BY e.startDate DESC")
	List<Event> findByStatusOrderByStartDateDesc(@Param("status") Event.EventStatus status);

	@Query("SELECT e FROM Event e WHERE e.isDeleted = false AND e.status = :status AND :startDate <= e.endDate AND :endDate >= e.startDate")
	List<Event> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(@Param("status") Event.EventStatus status,
			@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	@Query("SELECT e FROM Event e WHERE e.isDeleted = false AND e.status = :status AND :now BETWEEN e.startDate AND e.endDate")
	List<Event> findActiveEvents(@Param("status") Event.EventStatus status, @Param("now") LocalDateTime now);

	@Query("SELECT e FROM Event e LEFT JOIN FETCH e.discounts LEFT JOIN FETCH e.products WHERE e.isDeleted = false AND e.id = :id")
	Optional<Event> findByIdWithDiscountsAndProducts(@Param("id") UUID id);

	@Query("SELECT e FROM Event e LEFT JOIN FETCH e.discounts WHERE e.isDeleted = false AND e.id = :id")
	Optional<Event> findByIdWithDiscounts(@Param("id") UUID id);

	@Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.discounts LEFT JOIN FETCH e.products WHERE e.isDeleted = false")
	Page<Event> findAllWithDiscountsAndProducts(Pageable pageable);

	@Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e WHERE e.isDeleted = false AND e.name = :name")
	boolean existsByName(@Param("name") String name);

	@Query("SELECT COUNT(e) > 0 FROM Event e " +
		   "WHERE e.isDeleted = false " +
		   "AND e.discounts IS NOT EMPTY " +
		   "AND e.status != 'INACTIVE' " +
		   "AND ((e.startDate <= :endDate AND e.endDate >= :startDate))")
	boolean existsActiveEventWithDiscountInDateRange(@Param("startDate") LocalDateTime startDate, 
													@Param("endDate") LocalDateTime endDate);

	@Query("SELECT COUNT(e) > 0 FROM Event e " +
		   "WHERE e.isDeleted = false " +
		   "AND e.id != :eventId " +
		   "AND e.discounts IS NOT EMPTY " +
		   "AND e.status != 'INACTIVE' " +
		   "AND ((e.startDate <= :endDate AND e.endDate >= :startDate))")
	boolean existsActiveEventWithDiscountInDateRangeExcludingEvent(@Param("eventId") UUID eventId,
																   @Param("startDate") LocalDateTime startDate,
																   @Param("endDate") LocalDateTime endDate);

	@Query("SELECT DISTINCT e FROM Event e " +
		   "JOIN FETCH e.discounts d " +
		   "JOIN e.products p " +
		   "WHERE e.isDeleted = false " +
		   "AND p.id IN :productIds " +
		   "AND e.status = 'ACTIVE' " +
		   "AND :now BETWEEN e.startDate AND e.endDate")
	List<Event> findActiveEventsWithDiscountsForProducts(@Param("productIds") List<UUID> productIds, 
														  @Param("now") LocalDateTime now);

	@Query("SELECT e FROM Event e WHERE e.isDeleted = false AND e.id = :id")
	Optional<Event> findByIdAndNotDeleted(@Param("id") UUID id);
}
