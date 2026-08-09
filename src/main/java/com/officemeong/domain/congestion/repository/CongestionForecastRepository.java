package com.officemeong.domain.congestion.repository;

import com.officemeong.domain.congestion.entity.CongestionForecast;
import com.officemeong.domain.place.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CongestionForecastRepository extends JpaRepository<CongestionForecast, Long> {

    Optional<CongestionForecast> findByTatsNmAndBaseYmd(String tatsNm, LocalDate baseYmd);

    List<CongestionForecast> findByRegionAndBaseYmd(Region region, LocalDate baseYmd);

    List<CongestionForecast> findByRegionAndBaseYmdBetween(Region region, LocalDate from, LocalDate to);

    @Query("SELECT AVG(c.cnctrRate) FROM CongestionForecast c WHERE c.region = :region AND c.baseYmd = :date")
    Double findAvgRateByRegionAndDate(@Param("region") Region region,
                                       @Param("date") LocalDate date);
}
