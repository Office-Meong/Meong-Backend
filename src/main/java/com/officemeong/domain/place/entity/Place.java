package com.officemeong.domain.place.entity;

import com.officemeong.common.entity.BaseTimeEntity;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.enums.SourceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "places",
    uniqueConstraints = @UniqueConstraint(name = "uq_source", columnNames = {"source_type", "source_id"}),
    indexes = {
        @Index(name = "idx_region_type", columnList = "region, place_type"),
        @Index(name = "idx_coords", columnList = "latitude, longitude")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 10)
    private SourceType sourceType;

    @Column(name = "source_id", nullable = false, length = 50)
    private String sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type", nullable = false, length = 20)
    private PlaceType placeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Region region;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 50)
    private String tel;

    @Column(length = 500)
    private String homepage;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @OneToOne(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlacePetCondition petCondition;

    @OneToOne(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceOperation operation;

    @OneToOne(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceAccessibility accessibility;

    @OneToOne(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceScore score;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("display_order ASC")
    private List<PlaceImage> images = new ArrayList<>();

    @Builder
    public Place(SourceType sourceType, String sourceId, PlaceType placeType, Region region,
                 String name, String address, BigDecimal latitude, BigDecimal longitude,
                 String tel, String homepage, String overview) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.placeType = placeType;
        this.region = region;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tel = tel;
        this.homepage = homepage;
        this.overview = overview;
        this.lastSyncedAt = LocalDateTime.now();
    }

    public void updateSyncTime() {
        this.lastSyncedAt = LocalDateTime.now();
    }

    public void update(String name, String address, BigDecimal latitude, BigDecimal longitude,
                       String tel, String homepage, String overview) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tel = tel;
        this.homepage = homepage;
        this.overview = overview;
        this.lastSyncedAt = LocalDateTime.now();
    }
}
