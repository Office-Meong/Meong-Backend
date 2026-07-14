package com.officemeong.domain.place.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CongestionLevel {
    RELAXED(12, 15),
    NORMAL(9, 11),
    CROWDED(6, 8),
    VERY_CROWDED(0, 5);

    private final int minScore;
    private final int maxScore;
}
