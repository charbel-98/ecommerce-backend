package com.charbel.ecommerce.category.repository;

import com.charbel.ecommerce.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

	Optional<Category> findBySlug(String slug);

	boolean existsByName(String name);

	boolean existsBySlug(String slug);

	@Query("SELECT c FROM Category c WHERE c.parentId IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
	List<Category> findRootCategories();

	@Query("SELECT c FROM Category c WHERE c.parentId = :parentId AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
	List<Category> findByParentId(@Param("parentId") UUID parentId);

	@Query("SELECT c FROM Category c WHERE c.level = :level AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
	List<Category> findByLevel(@Param("level") Integer level);

	List<Category> findByIsActiveTrueOrderBySortOrderAscNameAsc();

	@Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.level ASC, c.sortOrder ASC, c.name ASC")
	List<Category> findAllActiveHierarchical();
}
