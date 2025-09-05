package com.charbel.ecommerce.product.repository;

import com.charbel.ecommerce.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
	
	@Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product WHERE pv.stock < :threshold")
	List<ProductVariant> findLowStockVariants(int threshold);
}