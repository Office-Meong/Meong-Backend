package com.officemeong.domain.dog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "반려견 등록/수정 요청")
@Getter
@NoArgsConstructor
public class DogRequest {

    @Schema(description = "반려견 이름 (필수, 최대 50자)", example = "뽀삐", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50)
    private String name;

    @Schema(description = "품종 (선택, 최대 100자)", example = "말티즈")
    @Size(max = 100)
    private String breed;

    @Schema(description = "몸무게 kg (선택, 0 이상)", example = "3.5")
    @PositiveOrZero
    private BigDecimal weightKg;

    @Schema(description = "생년월일 (선택, yyyy-MM-dd 형식)", example = "2021-03-15")
    private LocalDate birthDate;

    @Schema(description = "중성화 여부 (선택)", example = "false")
    private Boolean isNeutered;
}
