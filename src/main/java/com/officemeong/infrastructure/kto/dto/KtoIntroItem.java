package com.officemeong.infrastructure.kto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * KTO detailIntro2 응답. contentTypeId에 따라 활성 필드가 다름.
 * 숙박(32): checkintime, checkouttime, parking
 * 음식점(39)/카페: opentimefood, restdatefood, parkingfood
 * 관광지(12): usetime, restdate, parking
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KtoIntroItem {

    private String contentid;
    private String contenttypeid;

    // 숙박(32)
    private String checkintime;
    private String checkouttime;
    private String parking;

    // 음식점/카페(39)
    private String opentimefood;
    private String restdatefood;
    private String parkingfood;

    // 관광지(12)
    private String usetime;
    private String restdate;

    // 문화시설(14), 레포츠(28)
    private String usetimeculture;
    private String restdateculture;
    private String parkingculture;

    public String getOperatingHours() {
        if (isNotEmpty(usetime)) return usetime;
        if (isNotEmpty(opentimefood)) return opentimefood;
        if (isNotEmpty(usetimeculture)) return usetimeculture;
        if (isNotEmpty(checkintime)) return "체크인 " + checkintime + " / 체크아웃 " + checkouttime;
        return null;
    }

    public String getClosedDays() {
        if (isNotEmpty(restdate)) return restdate;
        if (isNotEmpty(restdatefood)) return restdatefood;
        if (isNotEmpty(restdateculture)) return restdateculture;
        return null;
    }

    public Boolean getParkingAvailable() {
        String p = isNotEmpty(parking) ? parking :
                   isNotEmpty(parkingfood) ? parkingfood :
                   isNotEmpty(parkingculture) ? parkingculture : null;
        if (p == null) return null;
        return p.contains("가능") || p.contains("있음") || p.contains("주차");
    }

    private boolean isNotEmpty(String s) {
        return s != null && !s.isBlank();
    }
}
