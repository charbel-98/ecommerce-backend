package com.charbel.ecommerce.review.repository;

import com.charbel.ecommerce.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, UUID> {
    
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.isDeleted = false AND ri.reviewId = :reviewId ORDER BY ri.sortOrder ASC")
    List<ReviewImage> findByReviewIdOrderBySortOrderAsc(@Param("reviewId") UUID reviewId);
    
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.isDeleted = false AND ri.reviewId = :reviewId ORDER BY ri.sortOrder ASC")
    List<ReviewImage> findByReviewIdSorted(@Param("reviewId") UUID reviewId);
    
    @Modifying
    @Query("UPDATE ReviewImage ri SET ri.isDeleted = true WHERE ri.reviewId = :reviewId")
    void deleteByReviewId(@Param("reviewId") UUID reviewId);

    @Query("SELECT ri FROM ReviewImage ri WHERE ri.isDeleted = false AND ri.id = :id")
    Optional<ReviewImage> findByIdAndNotDeleted(@Param("id") UUID id);
}