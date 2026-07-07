package com.officemeong.infrastructure.gwto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GwtoListItem {

    private String contentSeq;
    private String areaName;
    private String partName;
    private String title;
    private String address;
    private String latitude;
    private String longitude;
    private String tel;
    private String totalCount;
}
