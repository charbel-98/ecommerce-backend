package com.charbel.ecommerce.orders.repository;

import com.charbel.ecommerce.orders.entity.Order;
import com.charbel.ecommerce.orders.entity.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.isDeleted = false")
	List<Order> findAllOrdersWithDetails();

	@Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o JOIN o.orderItems oi WHERE o.isDeleted = false AND o.user.id = :userId AND oi.variant.product.id = :productId AND o.status = 'COMPLETED'")
	boolean existsByUserIdAndProductId(@Param("userId") UUID userId, @Param("productId") UUID productId);

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.isDeleted = false AND o.user.id = :userId ORDER BY o.createdAt DESC")
	List<Order> findByUserIdWithDetails(@Param("userId") UUID userId);

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.isDeleted = false AND o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
	List<Order> findByUserIdAndStatusWithDetails(@Param("userId") UUID userId, @Param("status") OrderStatus status);

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.isDeleted = false AND o.user.id = :userId AND o.status IN :statuses ORDER BY o.createdAt DESC")
	List<Order> findByUserIdAndStatusInWithDetails(@Param("userId") UUID userId, @Param("statuses") List<OrderStatus> statuses);

	@Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.isDeleted = false AND o.orderNumber = :orderNumber")
	boolean existsByOrderNumber(@Param("orderNumber") String orderNumber);

	@Query("SELECT o FROM Order o WHERE o.isDeleted = false AND o.id = :id")
	Optional<Order> findByIdAndNotDeleted(@Param("id") UUID id);

	// Paginated queries
	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.isDeleted = false ORDER BY o.createdAt DESC")
	Page<Order> findAllOrdersWithDetailsPaginated(Pageable pageable);

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.isDeleted = false AND o.user.id = :userId ORDER BY o.createdAt DESC")
	Page<Order> findByUserIdWithDetailsPaginated(@Param("userId") UUID userId, Pageable pageable);

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.isDeleted = false AND o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
	Page<Order> findByUserIdAndStatusWithDetailsPaginated(@Param("userId") UUID userId, @Param("status") OrderStatus status, Pageable pageable);

	@Query("SELECT DISTINCT o FROM Order o " +
		   "JOIN FETCH o.user " +
		   "JOIN FETCH o.address " +
		   "JOIN FETCH o.orderItems oi " +
		   "JOIN FETCH oi.variant v " +
		   "JOIN FETCH v.product p " +
		   "WHERE o.isDeleted = false AND o.user.id = :userId AND o.status IN :statuses ORDER BY o.createdAt DESC")
	Page<Order> findByUserIdAndStatusInWithDetailsPaginated(@Param("userId") UUID userId, @Param("statuses") List<OrderStatus> statuses, Pageable pageable);
}