package com.officemeong.infrastructure.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoLocalSearchResponse {

    private List<Document> documents;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private String id;
        @JsonProperty("place_name")
        private String placeName;
        @JsonProperty("category_name")
        private String categoryName;
        @JsonProperty("category_group_code")
        private String categoryGroupCode;
        private String phone;
        @JsonProperty("address_name")
        private String addressName;
        @JsonProperty("road_address_name")
        private String roadAddressName;
        private String x; // 경도
        private String y; // 위도
        @JsonProperty("place_url")
        private String placeUrl;
    }
}
