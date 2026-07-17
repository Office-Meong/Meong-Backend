package com.officemeong.domain.dog.dto;

import com.officemeong.domain.dog.entity.Dog;
import com.officemeong.domain.dog.enums.ActivityLevel;
import com.officemeong.domain.dog.enums.DogSize;
import com.officemeong.domain.dog.enums.HealthStatus;
import com.officemeong.domain.dog.enums.Sociability;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DogResponse {

    private Long id;
    private String name;
    private String breed;
    private BigDecimal weightKg;
    private LocalDate birthDate;
    private Boolean isNeutered;
    private String imageUrl;
    private DogSize sizeCategory;
    private ActivityLevel activityLevel;
    private Sociability sociability;
    private HealthStatus healthStatus;

    public static DogResponse from(Dog dog) {
        return DogResponse.builder()
                .id(dog.getId())
                .name(dog.getName())
                .breed(dog.getBreed())
                .weightKg(dog.getWeightKg())
                .birthDate(dog.getBirthDate())
                .isNeutered(dog.getIsNeutered())
                .imageUrl(dog.getImageUrl())
                .sizeCategory(dog.getSizeCategory())
                .activityLevel(dog.getActivityLevel())
                .sociability(dog.getSociability())
                .healthStatus(dog.getHealthStatus())
                .build();
    }
}
