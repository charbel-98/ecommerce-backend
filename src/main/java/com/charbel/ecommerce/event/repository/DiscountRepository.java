package com.charbel.ecommerce.event.repository;

import com.charbel.ecommerce.event.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, UUID> {

	List<Discount> findByEventId(UUID eventId);

	void deleteByEventId(UUID eventId);
}
