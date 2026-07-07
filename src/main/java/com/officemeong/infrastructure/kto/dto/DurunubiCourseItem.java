package com.officemeong.infrastructure.kto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DurunubiCourseItem {

    private String crsIdx;
    private String routeIdx;
    private String crsKorNm;
    private String crsSummary;
    private String crsContents;
    private String crsDstnc;
    private String crsTotlRqrmHour;
    private String crsLevel;
    private String crsCycle;
    private String sigun;
    private String gpxpath;
    private String brdDiv;
    private String createdtime;
    private String modifiedtime;
}
