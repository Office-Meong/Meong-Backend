package com.officemeong.domain.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_images",
    indexes = @Index(name = "idx_place", columnList = "place_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "is_thumbnail")
    private boolean isThumbnail = false;

    @Column(name = "display_order")
    private int displayOrder = 0;

    @Builder
    public PlaceImage(Place place, String imageUrl, boolean isThumbnail, int displayOrder) {
        this.place = place;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
        this.displayOrder = displayOrder;
    }
}
