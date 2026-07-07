package com.officemeong.domain.place.repository;

import com.officemeong.domain.place.entity.PlaceScore;
import com.officemeong.domain.place.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceScoreRepository extends JpaRepository<PlaceScore, Long> {

    @Query("SELECT s FROM PlaceScore s WHERE s.place.region = :region")
    List<PlaceScore> findByPlaceRegion(@Param("region") Region region);
}
