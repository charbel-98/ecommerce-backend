package com.charbel.ecommerce.review.repository;

import com.charbel.ecommerce.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, UUID> {
    
    List<ReviewImage> findByReviewIdOrderBySortOrderAsc(UUID reviewId);
    
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.reviewId = :reviewId ORDER BY ri.sortOrder ASC")
    List<ReviewImage> findByReviewIdSorted(@Param("reviewId") UUID reviewId);
    
    void deleteByReviewId(UUID reviewId);
}