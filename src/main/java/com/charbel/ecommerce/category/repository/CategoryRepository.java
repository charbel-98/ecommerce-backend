package com.charbel.ecommerce.category.repository;

import com.charbel.ecommerce.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

	@Query("SELECT c FROM Category c WHERE c.isDeleted = false AND c.slug = :slug")
	Optional<Category> findBySlug(@Param("slug") String slug);

	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.isDeleted = false AND c.name = :name")
	boolean existsByName(@Param("name") String name);

	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.isDeleted = false AND c.slug = :slug")
	boolean existsBySlug(@Param("slug") String slug);

	@Query("SELECT c FROM Category c WHERE c.isDeleted = false AND c.parentId IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
	List<Category> findRootCategories();

	@Query("SELECT c FROM Category c WHERE c.isDeleted = false AND c.parentId = :parentId AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
	List<Category> findByParentId(@Param("parentId") UUID parentId);

	@Query("SELECT c FROM Category c WHERE c.isDeleted = false AND c.level = :level AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
	List<Category> findByLevel(@Param("level") Integer level);

	@Query("SELECT c FROM Category c WHERE c.isDeleted = false AND c.isActive = true ORDER BY c.sortOrder ASC, c.name ASC")
	List<Category> findByIsActiveTrueOrderBySortOrderAscNameAsc();

	@Query("SELECT c FROM Category c WHERE c.isDeleted = false AND c.isActive = true ORDER BY c.level ASC, c.sortOrder ASC, c.name ASC")
	List<Category> findAllActiveHierarchical();

	@Query("SELECT c FROM Category c WHERE c.isDeleted = false AND c.isActive = true AND c.id NOT IN "
			+ "(SELECT DISTINCT p.id FROM Category p WHERE p.isDeleted = false AND p.id IN "
			+ "(SELECT ch.parentId FROM Category ch WHERE ch.isDeleted = false AND ch.parentId IS NOT NULL AND ch.isActive = true)) "
			+ "ORDER BY c.sortOrder ASC, c.name ASC")
	Page<Category> findLeafCategoriesPageable(Pageable pageable);

	@Query("SELECT c FROM Category c WHERE c.isDeleted = false AND c.id = :id")
	Optional<Category> findByIdAndNotDeleted(@Param("id") UUID id);

	@Query("SELECT c FROM Category c WHERE c.isDeleted = false ORDER BY c.sortOrder ASC, c.name ASC")
	List<Category> findAllAndNotDeleted();
}
