package com.officemeong.domain.dog.dto;

import com.officemeong.domain.dog.entity.Dog;
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

    public static DogResponse from(Dog dog) {
        return DogResponse.builder()
                .id(dog.getId())
                .name(dog.getName())
                .breed(dog.getBreed())
                .weightKg(dog.getWeightKg())
                .birthDate(dog.getBirthDate())
                .isNeutered(dog.getIsNeutered())
                .build();
    }
}
