package com.charbel.ecommerce.product.repository;

import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category")
	Page<Product> findAllProductsWithVariants(Pageable pageable);

	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category "
			+ "JOIN p.events e WHERE e.id = :eventId")
	Page<Product> findProductsByEventId(@Param("eventId") UUID eventId, Pageable pageable);

	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category "
			+ "WHERE p.categoryId = :categoryId AND p.status = 'ACTIVE'")
	Page<Product> findProductsByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category "
			+ "WHERE p.categoryId = :categoryId AND p.status = 'ACTIVE' "
			+ "ORDER BY "
			+ "CASE WHEN :sortType = 'PRICE_HIGH_TO_LOW' THEN p.basePrice END DESC, "
			+ "CASE WHEN :sortType = 'PRICE_LOW_TO_HIGH' THEN p.basePrice END ASC, "
			+ "CASE WHEN :sortType = 'REVIEWS' THEN p.averageRating END DESC, "
			+ "CASE WHEN :sortType = 'REVIEWS' THEN p.reviewCount END DESC, "
			+ "CASE WHEN :sortType = 'NEWEST' THEN p.createdAt END DESC, "
			+ "p.id ASC")
	Page<Product> findProductsByCategoryIdWithSort(@Param("categoryId") UUID categoryId, 
												   @Param("sortType") String sortType, 
												   Pageable pageable);

	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants LEFT JOIN FETCH p.brand b LEFT JOIN FETCH p.category "
			+ "WHERE b.slug = :brandSlug AND p.status = 'ACTIVE' AND b.status = 'ACTIVE'")
	Page<Product> findProductsByBrandSlug(@Param("brandSlug") String brandSlug, Pageable pageable);

	@Query("SELECT DISTINCT e FROM Event e " +
		   "JOIN e.products p " +
		   "JOIN e.discounts d " +
		   "WHERE p.id = :productId " +
		   "AND e.status = 'ACTIVE' " +
		   "AND :currentTime BETWEEN e.startDate AND e.endDate")
	List<Event> findActiveEventsWithDiscountsForProduct(@Param("productId") UUID productId, 
														@Param("currentTime") LocalDateTime currentTime);
}
