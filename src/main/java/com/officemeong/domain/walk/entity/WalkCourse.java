package com.officemeong.domain.walk.entity;

import com.officemeong.domain.place.enums.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "walk_courses",
    indexes = {
        @Index(name = "idx_region", columnList = "region"),
        @Index(name = "idx_coords", columnList = "start_latitude, start_longitude")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_id", length = 50)
    private String sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Region region;

    @Column(name = "course_name", length = 200)
    private String courseName;

    @Column(name = "start_latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal startLatitude;

    @Column(name = "start_longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal startLongitude;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public WalkCourse(String sourceId, Region region, String courseName,
                      BigDecimal startLatitude, BigDecimal startLongitude, BigDecimal distanceKm) {
        this.sourceId = sourceId;
        this.region = region;
        this.courseName = courseName;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.distanceKm = distanceKm;
        this.lastSyncedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
}
