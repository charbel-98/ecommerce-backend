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
    
    Optional<Brand> findBySlug(String slug);
    
    boolean existsByName(String name);
    
    boolean existsBySlug(String slug);
    
    @Query("SELECT b FROM Brand b WHERE b.status = 'ACTIVE' ORDER BY b.name ASC")
    List<Brand> findAllActiveBrands();
    
    List<Brand> findByStatusOrderByNameAsc(Brand.BrandStatus status);
}