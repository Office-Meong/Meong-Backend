package com.officemeong.infrastructure.gwto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GwtoDetailItem {

    private String contentSeq;
    private String title;
    private String partName;
    private String areaName;
    private String address;
    private String latitude;
    private String longitude;
    private String tel;
    private String homePage;
    private String content;
    private String keyword;

    // 운영 정보
    private String usedTime;
    private String usedCost;
    private String mainFacility;
    private String parkingFlag;
    private String parkingLog;

    // 반려동물 조건
    private String petFlag;
    private String inOutFlag;
    private String petWeight;
    private String dogBreed;
    private String petFacility;
    private String policyCautions;
    private String bathFlag;
    private String catFlag;
    private String entranceFlag;

    // 기타
    private String provisionSupply;
    private String provisionFlag;
    private String emergencyResponse;
    private String emergencyFlag;
    private String memo;

    private List<ImageItem> imageList;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageItem {
        private String image;
        private String thumbnail;
    }

    public boolean isParkingAvailable() {
        return "Y".equalsIgnoreCase(parkingFlag);
    }

    public boolean isCafeKeyword() {
        return keyword != null && keyword.contains("카페");
    }
}
