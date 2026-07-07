package com.officemeong.domain.place.enums;

public enum Region {
    GANGNEUNG,
    CHUNCHEON,
    WONJU;

    public String getKtoAreaCode() {
        return "32";
    }

    public String getKtoSigunguCode() {
        return switch (this) {
            case GANGNEUNG -> "1";
            case CHUNCHEON -> "2";
            case WONJU -> "3";
        };
    }

    public String getGwtoAreaCode() {
        return switch (this) {
            case GANGNEUNG -> "AC03";
            case CHUNCHEON -> "AC01";
            case WONJU -> "AC02";
        };
    }

    public String getCongestionAreaCd() {
        return "51";
    }

    public String getCongestionSignguCd() {
        return switch (this) {
            case GANGNEUNG -> "51150";
            case CHUNCHEON -> "51110";
            case WONJU -> "51130";
        };
    }

    public String getDurunubiSigun() {
        return switch (this) {
            case GANGNEUNG -> "강원 강릉시";
            case CHUNCHEON -> "강원 춘천시";
            case WONJU -> "강원 원주시";
        };
    }
}
