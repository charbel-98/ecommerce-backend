package com.charbel.ecommerce.event.repository;

import com.charbel.ecommerce.event.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, UUID> {

	@Query("SELECT d FROM Discount d WHERE d.isDeleted = false AND d.eventId = :eventId")
	List<Discount> findByEventId(@Param("eventId") UUID eventId);

	@Modifying
	@Query("UPDATE Discount d SET d.isDeleted = true WHERE d.eventId = :eventId")
	void deleteByEventId(@Param("eventId") UUID eventId);

	@Query("SELECT d FROM Discount d WHERE d.isDeleted = false AND d.id = :id")
	Optional<Discount> findByIdAndNotDeleted(@Param("id") UUID id);
}