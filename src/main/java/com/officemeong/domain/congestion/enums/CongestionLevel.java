package com.officemeong.domain.congestion.enums;

public enum CongestionLevel {
    RELAXED,
    NORMAL,
    CROWDED,
    VERY_CROWDED,
    UNKNOWN;

    public static CongestionLevel fromScore(int congestionScore) {
        if (congestionScore == 0) return UNKNOWN;
        if (congestionScore >= 12) return RELAXED;
        if (congestionScore >= 9) return NORMAL;
        if (congestionScore >= 6) return CROWDED;
        return VERY_CROWDED;
    }
}
