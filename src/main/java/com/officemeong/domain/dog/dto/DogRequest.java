package com.officemeong.domain.dog.dto;

import com.officemeong.domain.dog.enums.ActivityLevel;
import com.officemeong.domain.dog.enums.DogSize;
import com.officemeong.domain.dog.enums.HealthStatus;
import com.officemeong.domain.dog.enums.Sociability;
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

    @Schema(description = "반려견 사진 URL (선택)", example = "https://example.com/dog.jpg")
    private String imageUrl;

    @Schema(description = "반려견 크기 (SMALL: 소형 / MEDIUM: 중형 / LARGE: 대형)", example = "SMALL")
    private DogSize sizeCategory;

    @Schema(description = "활동량 (LOW: 낮음 / MEDIUM: 보통 / HIGH: 활발)", example = "MEDIUM")
    private ActivityLevel activityLevel;

    @Schema(description = "사회성 (FRIENDLY: 친화적 / NORMAL: 보통 / SENSITIVE: 예민함)", example = "FRIENDLY")
    private Sociability sociability;

    @Schema(description = "건강 상태 (HEALTHY: 건강함 / HAS_CONDITION: 지병있음 / RECENT_TREATMENT: 최근수술및치료중)", example = "HEALTHY")
    private HealthStatus healthStatus;
}
