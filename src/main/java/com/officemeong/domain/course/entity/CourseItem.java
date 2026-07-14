package com.officemeong.domain.course.entity;

import com.officemeong.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(
    name = "course_items",
    indexes = @Index(name = "idx_ci_course_id", columnList = "course_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "slot_label", length = 20)
    private String slotLabel;

    @Column(name = "distance_from_prev_km", precision = 6, scale = 2)
    private BigDecimal distanceFromPrevKm;

    @Builder
    public CourseItem(Course course, Place place, Integer dayNumber, Integer visitOrder,
                      LocalTime startTime, LocalTime endTime, String slotLabel,
                      BigDecimal distanceFromPrevKm) {
        this.course = course;
        this.place = place;
        this.dayNumber = dayNumber;
        this.visitOrder = visitOrder;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotLabel = slotLabel;
        this.distanceFromPrevKm = distanceFromPrevKm;
    }

    public void updateTime(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void updatePlace(Place place, BigDecimal distanceFromPrevKm) {
        this.place = place;
        this.distanceFromPrevKm = distanceFromPrevKm;
    }
}
