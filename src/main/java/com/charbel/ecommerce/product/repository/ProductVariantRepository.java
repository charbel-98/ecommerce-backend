package com.charbel.ecommerce.product.repository;

import com.charbel.ecommerce.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

	@Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product WHERE pv.isDeleted = false AND pv.stock < :threshold")
	List<ProductVariant> findLowStockVariants(@Param("threshold") int threshold);

	@Query("SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END FROM ProductVariant pv WHERE pv.isDeleted = false AND pv.sku = :sku")
	boolean existsBySku(@Param("sku") String sku);

	@Query("SELECT pv.sku FROM ProductVariant pv WHERE pv.isDeleted = false AND pv.sku IN :skus")
	Set<String> findExistingSkus(@Param("skus") Set<String> skus);

	@Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product WHERE pv.isDeleted = false AND pv.id IN :variantIds")
	List<ProductVariant> findByIdInWithProduct(@Param("variantIds") List<UUID> variantIds);

	@Query("SELECT pv FROM ProductVariant pv WHERE pv.isDeleted = false AND pv.id = :id")
	Optional<ProductVariant> findByIdAndNotDeleted(@Param("id") UUID id);

	@Query("SELECT pv FROM ProductVariant pv WHERE pv.isDeleted = false ORDER BY pv.createdAt DESC")
	List<ProductVariant> findAllAndNotDeleted();
}