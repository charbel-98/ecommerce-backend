package com.charbel.ecommerce.product.repository;

import com.charbel.ecommerce.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

	@Query("SELECT pi FROM ProductImage pi WHERE pi.isDeleted = false AND pi.productId = :productId AND pi.variantId IS NULL")
	List<ProductImage> findByProductIdAndVariantIdIsNull(@Param("productId") UUID productId);

	@Query("SELECT pi FROM ProductImage pi WHERE pi.isDeleted = false AND pi.variantId = :variantId")
	List<ProductImage> findByVariantId(@Param("variantId") UUID variantId);

	@Query("SELECT pi FROM ProductImage pi WHERE pi.isDeleted = false AND pi.id = :id")
	Optional<ProductImage> findByIdAndNotDeleted(@Param("id") UUID id);
}