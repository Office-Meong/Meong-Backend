package com.officemeong.domain.place.repository;

import com.officemeong.domain.place.entity.PlaceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {

    @Modifying
    @Query("DELETE FROM PlaceImage i WHERE i.place.id = :placeId")
    void deleteByPlaceId(Long placeId);
}
