package com.officemeong.domain.place.enums;

public enum Grade {
    A, B, C, D, E;

    public static Grade from(int totalScore) {
        if (totalScore >= 85) return A;
        if (totalScore >= 70) return B;
        if (totalScore >= 55) return C;
        if (totalScore >= 40) return D;
        return E;
    }
}
