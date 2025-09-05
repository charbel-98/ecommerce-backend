package com.charbel.ecommerce.product.repository;

import com.charbel.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
	
	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants")
	Page<Product> findAllProductsWithVariants(Pageable pageable);
}