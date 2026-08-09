package com.officemeong.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "페이지네이션 응답 래퍼")
@Getter
public class PageResponse<T> {

    @Schema(description = "현재 페이지의 데이터 목록")
    private final List<T> content;

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private final int page;

    @Schema(description = "페이지당 항목 수", example = "20")
    private final int size;

    @Schema(description = "전체 항목 수", example = "134")
    private final long totalElements;

    @Schema(description = "전체 페이지 수", example = "7")
    private final int totalPages;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private final boolean hasNext;

    private PageResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean hasNext) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages, (long) (page + 1) * size < totalElements);
    }
}
