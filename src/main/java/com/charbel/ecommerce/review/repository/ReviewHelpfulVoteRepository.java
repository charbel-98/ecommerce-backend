package com.charbel.ecommerce.review.repository;

import com.charbel.ecommerce.review.entity.ReviewHelpfulVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewHelpfulVoteRepository extends JpaRepository<ReviewHelpfulVote, UUID> {

    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);
    
    void deleteByReviewId(UUID reviewId);
}