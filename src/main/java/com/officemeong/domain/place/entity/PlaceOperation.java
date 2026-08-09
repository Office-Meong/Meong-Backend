package com.officemeong.domain.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_operations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceOperation {

    @Id
    private Long placeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(name = "operating_hours", length = 500)
    private String operatingHours;

    @Column(name = "closed_days", length = 200)
    private String closedDays;

    @Column(name = "usage_fee", columnDefinition = "TEXT")
    private String usageFee;

    @Column(name = "parking_available")
    private Boolean parkingAvailable;

    @Column(name = "indoor_outdoor_type", length = 10)
    private String indoorOutdoorType;

    @Builder
    public PlaceOperation(Place place, String operatingHours, String closedDays,
                          String usageFee, Boolean parkingAvailable, String indoorOutdoorType) {
        this.place = place;
        this.operatingHours = operatingHours;
        this.closedDays = closedDays;
        this.usageFee = usageFee;
        this.parkingAvailable = parkingAvailable;
        this.indoorOutdoorType = indoorOutdoorType;
    }

    public void update(String operatingHours, String closedDays, String usageFee,
                        Boolean parkingAvailable, String indoorOutdoorType) {
        this.operatingHours = operatingHours;
        this.closedDays = closedDays;
        this.usageFee = usageFee;
        this.parkingAvailable = parkingAvailable;
        this.indoorOutdoorType = indoorOutdoorType;
    }
}
