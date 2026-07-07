package com.officemeong.infrastructure.kto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CongestionForecastItem {

    @com.fasterxml.jackson.annotation.JsonProperty("tAtsNm")
    private String tAtsNm;

    private String cnctrRate;
    private String baseYmd;
    private String areaCd;
    private String areaNm;
    private String signguCd;
    private String signguNm;
}
