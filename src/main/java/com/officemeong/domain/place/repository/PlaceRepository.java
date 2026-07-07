package com.officemeong.domain.place.repository;

import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.enums.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findBySourceTypeAndSourceId(SourceType sourceType, String sourceId);

    boolean existsBySourceTypeAndSourceId(SourceType sourceType, String sourceId);

    List<Place> findByRegionAndPlaceType(Region region, PlaceType placeType);

    List<Place> findByRegion(Region region);
}
