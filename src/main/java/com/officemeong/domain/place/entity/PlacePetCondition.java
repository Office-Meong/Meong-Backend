package com.officemeong.domain.place.entity;

import com.officemeong.domain.place.enums.AcmpyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_pet_conditions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlacePetCondition {

    @Id
    private Long placeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "place_id")
    private Place place;

    @Enumerated(EnumType.STRING)
    @Column(name = "acmpy_type", length = 20)
    private AcmpyType acmpyType;

    @Column(name = "is_cage_required")
    private Boolean isCageRequired = false;

    @Column(name = "is_leash_required")
    private Boolean isLeashRequired = false;

    @Column(name = "pet_weight_limit_kg")
    private Integer petWeightLimitKg;

    @Column(name = "cat_allowed")
    private Boolean catAllowed = false;

    @Column(name = "bath_available")
    private Boolean bathAvailable = false;

    @Column(name = "companion_conditions", columnDefinition = "TEXT")
    private String companionConditions;

    @Column(name = "available_facilities", columnDefinition = "TEXT")
    private String availableFacilities;

    @Column(columnDefinition = "TEXT")
    private String cautions;

    @Builder
    public PlacePetCondition(Place place, AcmpyType acmpyType, Boolean isCageRequired,
                              Boolean isLeashRequired, Integer petWeightLimitKg,
                              Boolean catAllowed, Boolean bathAvailable,
                              String companionConditions, String availableFacilities, String cautions) {
        this.place = place;
        this.acmpyType = acmpyType;
        this.isCageRequired = isCageRequired != null ? isCageRequired : false;
        this.isLeashRequired = isLeashRequired != null ? isLeashRequired : false;
        this.petWeightLimitKg = petWeightLimitKg;
        this.catAllowed = catAllowed != null ? catAllowed : false;
        this.bathAvailable = bathAvailable != null ? bathAvailable : false;
        this.companionConditions = companionConditions;
        this.availableFacilities = availableFacilities;
        this.cautions = cautions;
    }

    public void update(AcmpyType acmpyType, Integer petWeightLimitKg, Boolean catAllowed, Boolean bathAvailable,
                        String companionConditions, String availableFacilities, String cautions) {
        this.acmpyType = acmpyType;
        this.petWeightLimitKg = petWeightLimitKg;
        this.catAllowed = catAllowed != null ? catAllowed : false;
        this.bathAvailable = bathAvailable != null ? bathAvailable : false;
        this.companionConditions = companionConditions;
        this.availableFacilities = availableFacilities;
        this.cautions = cautions;
    }
}
