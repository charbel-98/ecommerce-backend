package com.charbel.ecommerce.review.repository;

import com.charbel.ecommerce.review.entity.ReviewHelpfulVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewHelpfulVoteRepository extends JpaRepository<ReviewHelpfulVote, UUID> {

    @Query("SELECT CASE WHEN COUNT(rv) > 0 THEN true ELSE false END FROM ReviewHelpfulVote rv WHERE rv.isDeleted = false AND rv.reviewId = :reviewId AND rv.userId = :userId")
    boolean existsByReviewIdAndUserId(@Param("reviewId") UUID reviewId, @Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE ReviewHelpfulVote rv SET rv.isDeleted = true WHERE rv.reviewId = :reviewId")
    void deleteByReviewId(@Param("reviewId") UUID reviewId);

    @Query("SELECT rv FROM ReviewHelpfulVote rv WHERE rv.isDeleted = false AND rv.id = :id")
    Optional<ReviewHelpfulVote> findByIdAndNotDeleted(@Param("id") UUID id);
}