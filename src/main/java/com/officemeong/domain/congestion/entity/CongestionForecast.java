package com.officemeong.domain.congestion.entity;

import com.officemeong.domain.place.enums.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "congestion_forecasts",
    uniqueConstraints = @UniqueConstraint(name = "uq_forecast", columnNames = {"tats_nm", "base_ymd"}),
    indexes = @Index(name = "idx_region_date", columnList = "region, base_ymd")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CongestionForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tats_nm", nullable = false, length = 200)
    private String tatsNm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Region region;

    @Column(name = "area_cd", nullable = false, length = 10)
    private String areaCd;

    @Column(name = "signgu_cd", nullable = false, length = 10)
    private String signguCd;

    @Column(name = "base_ymd", nullable = false)
    private LocalDate baseYmd;

    @Column(name = "cnctr_rate", precision = 5, scale = 2)
    private BigDecimal cnctrRate;

    @Builder
    public CongestionForecast(String tatsNm, Region region, String areaCd,
                               String signguCd, LocalDate baseYmd, BigDecimal cnctrRate) {
        this.tatsNm = tatsNm;
        this.region = region;
        this.areaCd = areaCd;
        this.signguCd = signguCd;
        this.baseYmd = baseYmd;
        this.cnctrRate = cnctrRate;
    }

    public void updateRate(BigDecimal cnctrRate) {
        this.cnctrRate = cnctrRate;
    }
}
