package com.charbel.ecommerce.product.repository;

import com.charbel.ecommerce.event.entity.Event;
import com.charbel.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

	@Query("SELECT DISTINCT p FROM Product p " +
		   "LEFT JOIN FETCH p.variants v " +
		   "LEFT JOIN FETCH p.brand b " +
		   "LEFT JOIN FETCH p.category " +
		   "WHERE p.categoryId = :categoryId " +
		   "AND p.status = 'ACTIVE' " +
		   "AND b.status = 'ACTIVE' " +
		   "AND (:#{#brandSlugs == null} = true OR b.slug IN :brandSlugs) " +
		   "AND EXISTS (" +
		   "  SELECT 1 FROM ProductVariant pv " +
		   "  WHERE pv.product.id = p.id " +
		   "  AND (:minPrice IS NULL OR pv.price >= :minPrice) " +
		   "  AND (:maxPrice IS NULL OR pv.price <= :maxPrice) " +
		   "  AND (:#{#colors == null} = true OR " +
		   "       UPPER(FUNCTION('jsonb_extract_path_text', pv.attributes, 'color')) IN :colors) " +
		   "  AND (:#{#sizes == null} = true OR " +
		   "       UPPER(FUNCTION('jsonb_extract_path_text', pv.attributes, 'size')) IN :sizes)" +
		   ")")
	Page<Product> findFilteredProductsByCategoryId(@Param("categoryId") UUID categoryId,
												   @Param("minPrice") BigDecimal minPrice,
												   @Param("maxPrice") BigDecimal maxPrice,
												   @Param("colors") List<String> colors,
												   @Param("sizes") List<String> sizes,
												   @Param("brandSlugs") List<String> brandSlugs,
												   Pageable pageable);

	@Query("SELECT DISTINCT p FROM Product p " +
		   "LEFT JOIN FETCH p.variants v " +
		   "LEFT JOIN FETCH p.brand b " +
		   "LEFT JOIN FETCH p.category " +
		   "WHERE p.categoryId = :categoryId " +
		   "AND p.status = 'ACTIVE' " +
		   "AND b.status = 'ACTIVE' " +
		   "AND (:#{#brandSlugs == null} = true OR b.slug IN :brandSlugs) " +
		   "AND EXISTS (" +
		   "  SELECT 1 FROM ProductVariant pv " +
		   "  WHERE pv.product.id = p.id " +
		   "  AND (:minPrice IS NULL OR pv.price >= :minPrice) " +
		   "  AND (:maxPrice IS NULL OR pv.price <= :maxPrice) " +
		   "  AND (:#{#colors == null} = true OR " +
		   "       UPPER(FUNCTION('jsonb_extract_path_text', pv.attributes, 'color')) IN :colors) " +
		   "  AND (:#{#sizes == null} = true OR " +
		   "       UPPER(FUNCTION('jsonb_extract_path_text', pv.attributes, 'size')) IN :sizes)" +
		   ") " +
		   "ORDER BY " +
		   "CASE WHEN :sortType = 'PRICE_HIGH_TO_LOW' THEN p.basePrice END DESC, " +
		   "CASE WHEN :sortType = 'PRICE_LOW_TO_HIGH' THEN p.basePrice END ASC, " +
		   "CASE WHEN :sortType = 'REVIEWS' THEN p.averageRating END DESC, " +
		   "CASE WHEN :sortType = 'REVIEWS' THEN p.reviewCount END DESC, " +
		   "CASE WHEN :sortType = 'NEWEST' THEN p.createdAt END DESC, " +
		   "p.id ASC")
	Page<Product> findFilteredProductsByCategoryIdWithSort(@Param("categoryId") UUID categoryId,
														   @Param("minPrice") BigDecimal minPrice,
														   @Param("maxPrice") BigDecimal maxPrice,
														   @Param("colors") List<String> colors,
														   @Param("sizes") List<String> sizes,
														   @Param("brandSlugs") List<String> brandSlugs,
														   @Param("sortType") String sortType,
														   Pageable pageable);
}
