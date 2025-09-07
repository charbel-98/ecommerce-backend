package com.charbel.ecommerce.product.repository;

import com.charbel.ecommerce.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

	@Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product WHERE pv.stock < :threshold")
	List<ProductVariant> findLowStockVariants(int threshold);

	boolean existsBySku(String sku);

	@Query("SELECT pv.sku FROM ProductVariant pv WHERE pv.sku IN :skus")
	Set<String> findExistingSkus(Set<String> skus);

	@Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product WHERE pv.id IN :variantIds")
	List<ProductVariant> findByIdInWithProduct(List<UUID> variantIds);
}
