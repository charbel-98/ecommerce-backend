package com.charbel.ecommerce.review.repository;

import com.charbel.ecommerce.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query("SELECT r FROM Review r WHERE r.isDeleted = false AND r.productId = :productId ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdOrderByCreatedAtDesc(@Param("productId") UUID productId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.isDeleted = false AND r.productId = :productId AND r.userId = :userId")
    Optional<Review> findByProductIdAndUserId(@Param("productId") UUID productId, @Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.isDeleted = false AND r.productId = :productId AND r.userId = :userId")
    boolean existsByProductIdAndUserId(@Param("productId") UUID productId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.isDeleted = false AND r.productId = :productId")
    Long countByProductId(@Param("productId") UUID productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.isDeleted = false AND r.productId = :productId")
    BigDecimal findAverageRatingByProductId(@Param("productId") UUID productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.isDeleted = false AND r.productId = :productId GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> findRatingDistributionByProductId(@Param("productId") UUID productId);

    @Query("SELECT r FROM Review r WHERE r.isDeleted = false AND r.userId = :userId ORDER BY r.createdAt DESC")
    Page<Review> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.isDeleted = false AND r.productId = :productId AND r.rating = :rating ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdAndRatingOrderByCreatedAtDesc(@Param("productId") UUID productId, @Param("rating") Integer rating, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.isDeleted = false AND r.productId = :productId ORDER BY r.helpfulCount DESC, r.createdAt DESC")
    Page<Review> findByProductIdOrderByHelpfulCountDescAndCreatedAtDesc(@Param("productId") UUID productId, Pageable pageable);

    // Queries for reviews with images
    @Query("SELECT DISTINCT r FROM Review r INNER JOIN r.images WHERE r.isDeleted = false AND r.productId = :productId ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdWithImagesOrderByCreatedAtDesc(@Param("productId") UUID productId, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Review r INNER JOIN r.images WHERE r.isDeleted = false AND r.productId = :productId AND r.rating = :rating ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdAndRatingWithImagesOrderByCreatedAtDesc(@Param("productId") UUID productId, @Param("rating") Integer rating, Pageable pageable);

    @Query("SELECT DISTINCT r FROM Review r INNER JOIN r.images WHERE r.isDeleted = false AND r.productId = :productId ORDER BY r.helpfulCount DESC, r.createdAt DESC")
    Page<Review> findByProductIdWithImagesOrderByHelpfulCountDescAndCreatedAtDesc(@Param("productId") UUID productId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.isDeleted = false AND r.id = :id")
    Optional<Review> findByIdAndNotDeleted(@Param("id") UUID id);
}