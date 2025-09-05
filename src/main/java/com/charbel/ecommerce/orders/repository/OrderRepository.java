package com.charbel.ecommerce.orders.repository;

import com.charbel.ecommerce.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

	@Query("SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.orderItems oi JOIN FETCH oi.variant v JOIN FETCH v.product")
	List<Order> findAllOrdersWithDetails();
}