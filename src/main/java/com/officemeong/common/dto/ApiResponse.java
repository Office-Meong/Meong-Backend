package com.officemeong.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "공통 응답 래퍼. 모든 API 응답이 이 구조로 감싸져서 반환됩니다.")
@Getter
public class ApiResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "실제 응답 데이터. 실패 시 null")
    private final T data;

    @Schema(description = "오류 메시지. 성공 시 null", example = "요청 파라미터가 올바르지 않습니다.")
    private final String message;

    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
