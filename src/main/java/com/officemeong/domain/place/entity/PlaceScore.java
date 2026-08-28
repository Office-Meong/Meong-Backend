package com.officemeong.domain.place.entity;

import com.officemeong.domain.place.enums.Grade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "place_scores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceScore {

    @Id
    private Long placeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(name = "pet_companion_score")
    private int petCompanionScore = 0;

    @Column(name = "workcation_score")
    private int workcationScore = 0;

    @Column(name = "walk_accessibility_score")
    private int walkAccessibilityScore = 5;

    @Column(name = "congestion_score")
    private int congestionScore = 8;

    @Column(name = "emergency_score")
    private int emergencyScore = 0;

    @Column(name = "accessibility_score")
    private int accessibilityScore = 0;

    @Column(name = "total_score")
    private int totalScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private Grade grade;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Builder
    public PlaceScore(Place place, int petCompanionScore, int workcationScore,
                      int walkAccessibilityScore, int congestionScore,
                      int emergencyScore, int accessibilityScore) {
        this.place = place;
        this.petCompanionScore = petCompanionScore;
        this.workcationScore = workcationScore;
        this.walkAccessibilityScore = walkAccessibilityScore;
        this.congestionScore = congestionScore;
        this.emergencyScore = emergencyScore;
        this.accessibilityScore = accessibilityScore;
        recalculate();
    }

    public static PlaceScore createDefault(Place place) {
        return PlaceScore.builder()
                .place(place)
                .petCompanionScore(0)
                .workcationScore(0)
                .walkAccessibilityScore(5)
                .congestionScore(8)
                .emergencyScore(0)
                .accessibilityScore(0)
                .build();
    }

    public void recalculate() {
        this.totalScore = petCompanionScore + workcationScore + walkAccessibilityScore
                + congestionScore + emergencyScore + accessibilityScore;
        this.calculatedAt = LocalDateTime.now();
        // grade는 ScoreCalculateTasklet에서 PlaceType별 퍼센타일 기반으로 일괄 배분
    }

    public void updateGrade(Grade grade) {
        this.grade = grade;
    }

    public void updateCongestionScore(int score) {
        this.congestionScore = score;
        recalculate();
    }

    public void updateWalkAccessibilityScore(int score) {
        this.walkAccessibilityScore = score;
        recalculate();
    }

    public void updateAccessibilityScore(int score) {
        this.accessibilityScore = score;
        recalculate();
    }

    public void updateEmergencyScore(int score) {
        this.emergencyScore = score;
        recalculate();
    }

    public void updatePetAndWorkcationScores(int petScore, int workcationScore) {
        this.petCompanionScore = petScore;
        this.workcationScore = workcationScore;
        recalculate();
    }
}
