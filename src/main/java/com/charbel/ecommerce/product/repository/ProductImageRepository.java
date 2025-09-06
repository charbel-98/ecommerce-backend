package com.charbel.ecommerce.product.repository;

import com.charbel.ecommerce.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
	
	List<ProductImage> findByProductIdAndVariantIdIsNull(UUID productId);
	
	List<ProductImage> findByVariantId(UUID variantId);
}
