package com.officemeong.domain.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_accessibility")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceAccessibility {

    @Id
    private Long placeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(name = "has_parking")
    private Boolean hasParking = false;

    @Column(name = "stroller_accessible")
    private Boolean strollerAccessible = false;

    @Column(name = "has_ramp")
    private Boolean hasRamp = false;

    @Column(name = "data_available")
    private Boolean dataAvailable = false;

    @Builder
    public PlaceAccessibility(Place place, Boolean hasParking, Boolean strollerAccessible,
                               Boolean hasRamp, Boolean dataAvailable) {
        this.place = place;
        this.hasParking = hasParking != null ? hasParking : false;
        this.strollerAccessible = strollerAccessible != null ? strollerAccessible : false;
        this.hasRamp = hasRamp != null ? hasRamp : false;
        this.dataAvailable = dataAvailable != null ? dataAvailable : false;
    }

    public static PlaceAccessibility createDefault(Place place) {
        return PlaceAccessibility.builder()
                .place(place)
                .hasParking(false)
                .strollerAccessible(false)
                .hasRamp(false)
                .dataAvailable(false)
                .build();
    }
}
