package com.officemeong.domain.favorite.entity;

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
        name = "favorites",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_place_fav", columnNames = {"user_id", "place_id"}),
        indexes = @Index(name = "idx_fav_user_id", columnList = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Builder
    public Favorite(User user, Place place) {
        this.user = user;
        this.place = place;
    }
}
