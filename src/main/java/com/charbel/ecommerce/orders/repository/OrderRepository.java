package com.charbel.ecommerce.orders.repository;

import com.charbel.ecommerce.orders.entity.Order;
import com.charbel.ecommerce.orders.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p")
	List<Order> findAllOrdersWithDetails();

	@Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o JOIN o.orderItems oi WHERE o.user.id = :userId AND oi.variant.product.id = :productId AND o.status = 'COMPLETED'")
	boolean existsByUserIdAndProductId(UUID userId, UUID productId);

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
	List<Order> findByUserIdWithDetails(UUID userId);

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
	List<Order> findByUserIdAndStatusWithDetails(UUID userId, OrderStatus status);

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.user.id = :userId AND o.status IN :statuses ORDER BY o.createdAt DESC")
	List<Order> findByUserIdAndStatusInWithDetails(UUID userId, List<OrderStatus> statuses);

	boolean existsByOrderNumber(String orderNumber);
}
