package com.officemeong.domain.review.entity;

import com.officemeong.common.entity.BaseTimeEntity;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_place_review", columnNames = {"user_id", "place_id"}),
        indexes = {
                @Index(name = "idx_review_place_id", columnList = "place_id"),
                @Index(name = "idx_review_user_id", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, length = 500)
    private String content;

    @Builder
    public Review(User user, Place place, Integer score, String content) {
        this.user = user;
        this.place = place;
        this.score = score;
        this.content = content;
    }
}
