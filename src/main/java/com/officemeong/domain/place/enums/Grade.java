package com.officemeong.domain.place.enums;

public enum Grade {
    A, B, C, D, E;

    public static Grade from(int totalScore) {
        if (totalScore >= 75) return A;
        if (totalScore >= 60) return B;
        if (totalScore >= 45) return C;
        if (totalScore >= 30) return D;
        return E;
    }
}
