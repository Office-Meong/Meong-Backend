package com.officemeong.domain.dog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DogRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 100)
    private String breed;

    @PositiveOrZero
    private BigDecimal weightKg;

    private LocalDate birthDate;

    private Boolean isNeutered;
}
