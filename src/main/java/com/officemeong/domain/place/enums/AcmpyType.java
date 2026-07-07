package com.officemeong.domain.place.enums;

/**
 * 반려동물 동반 가능 공간 유형.
 * KTO acmpyTypeCd: "전구역 동반가능" → INDOOR_OUTDOOR, "일부구역 동반가능" → DESIGNATED
 * GWTO inOutFlag: IN → INDOOR, OUT → OUTDOOR, INOUT → INDOOR_OUTDOOR
 */
public enum AcmpyType {
    INDOOR,
    OUTDOOR,
    INDOOR_OUTDOOR,
    DESIGNATED,
    UNKNOWN;

    public static AcmpyType fromKto(String acmpyTypeCd) {
        if (acmpyTypeCd == null || acmpyTypeCd.isBlank()) return UNKNOWN;
        return switch (acmpyTypeCd.trim()) {
            case "전구역 동반가능" -> INDOOR_OUTDOOR;
            case "일부구역 동반가능" -> DESIGNATED;
            default -> UNKNOWN;
        };
    }

    public static AcmpyType fromGwtoInOutFlag(String inOutFlag) {
        if (inOutFlag == null || inOutFlag.isBlank()) return UNKNOWN;
        return switch (inOutFlag.trim().toUpperCase()) {
            case "IN" -> INDOOR;
            case "OUT" -> OUTDOOR;
            case "INOUT" -> INDOOR_OUTDOOR;
            default -> UNKNOWN;
        };
    }
}
