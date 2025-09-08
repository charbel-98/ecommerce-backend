package com.charbel.ecommerce.brand.repository;

import com.charbel.ecommerce.brand.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

	@Query("SELECT b FROM Brand b WHERE b.isDeleted = false AND b.slug = :slug")
	Optional<Brand> findBySlug(String slug);

	@Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Brand b WHERE b.isDeleted = false AND b.name = :name")
	boolean existsByName(String name);

	@Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Brand b WHERE b.isDeleted = false AND b.slug = :slug")
	boolean existsBySlug(String slug);

	@Query("SELECT b FROM Brand b WHERE b.isDeleted = false AND b.status = 'ACTIVE' ORDER BY b.name ASC")
	List<Brand> findAllActiveBrands();

	@Query("SELECT b FROM Brand b WHERE b.isDeleted = false AND b.status = :status ORDER BY b.name ASC")
	List<Brand> findByStatusOrderByNameAsc(Brand.BrandStatus status);

	@Query("SELECT b FROM Brand b WHERE b.isDeleted = false AND b.id = :id")
	Optional<Brand> findByIdAndNotDeleted(UUID id);
}
