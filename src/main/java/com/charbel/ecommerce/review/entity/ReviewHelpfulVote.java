package com.charbel.ecommerce.review.entity;

import com.charbel.ecommerce.common.entity.BaseEntity;
import com.charbel.ecommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "review_helpful_votes", indexes = {
    @Index(name = "idx_review_helpful_votes_review", columnList = "review_id"),
    @Index(name = "idx_review_helpful_votes_user", columnList = "user_id"),
    @Index(name = "idx_review_helpful_votes_unique", columnList = "review_id,user_id", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewHelpfulVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "review_id", insertable = false, updatable = false)
    private UUID reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

}