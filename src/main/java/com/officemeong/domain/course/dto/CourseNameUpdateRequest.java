package com.officemeong.domain.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "코스 이름 수정 요청")
@Getter
@NoArgsConstructor
public class CourseNameUpdateRequest {

    @Schema(description = "새 코스 이름 (최대 100자)", example = "우리 가족 강릉 여행", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "코스 이름은 필수입니다.")
    @Size(max = 100)
    private String name;
}
