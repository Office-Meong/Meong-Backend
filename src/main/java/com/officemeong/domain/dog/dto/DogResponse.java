package com.officemeong.domain.dog.dto;

import com.officemeong.domain.dog.entity.Dog;
import com.officemeong.domain.dog.enums.ActivityLevel;
import com.officemeong.domain.dog.enums.DogSize;
import com.officemeong.domain.dog.enums.HealthStatus;
import com.officemeong.domain.dog.enums.Sociability;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "반려견 정보 응답")
@Getter
@Builder
public class DogResponse {

    @Schema(description = "반려견 ID", example = "1")
    private Long id;

    @Schema(description = "반려견 이름", example = "뽀삐")
    private String name;

    @Schema(description = "품종", example = "말티즈")
    private String breed;

    @Schema(description = "몸무게(kg)", example = "3.5")
    private BigDecimal weightKg;

    @Schema(description = "생년월일", example = "2021-03-15")
    private LocalDate birthDate;

    @Schema(description = "중성화 여부", example = "false")
    private Boolean isNeutered;

    @Schema(description = "반려견 사진 URL", example = "https://example.com/dog.jpg")
    private String imageUrl;

    @Schema(description = "반려견 크기 (SMALL: 소형 / MEDIUM: 중형 / LARGE: 대형)", example = "SMALL")
    private DogSize sizeCategory;

    @Schema(description = "활동량 (LOW: 낮음 / MEDIUM: 보통 / HIGH: 활발)", example = "MEDIUM")
    private ActivityLevel activityLevel;

    @Schema(description = "사회성 (FRIENDLY: 친화적 / NORMAL: 보통 / SENSITIVE: 예민함)", example = "FRIENDLY")
    private Sociability sociability;

    @Schema(description = "건강 상태 (HEALTHY: 건강함 / HAS_CONDITION: 지병있음 / RECENT_TREATMENT: 최근수술및치료중)", example = "HEALTHY")
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
