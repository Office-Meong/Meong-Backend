package com.officemeong.domain.favorite.repository;

import com.officemeong.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.place p " +
           "WHERE f.user.id = :userId " +
           "AND (:region IS NULL OR p.region = :region) " +
           "AND (:placeType IS NULL OR p.placeType = :placeType) " +
           "ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdWithPlace(@Param("userId") Long userId,
                                         @Param("region") com.officemeong.domain.place.enums.Region region,
                                         @Param("placeType") com.officemeong.domain.place.enums.PlaceType placeType);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.place WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdWithPlace(@Param("userId") Long userId);

    Optional<Favorite> findByUserIdAndPlaceId(Long userId, Long placeId);

    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

    @Query("SELECT f.place.id FROM Favorite f WHERE f.user.id = :userId AND f.place.id IN :placeIds")
    List<Long> findFavoritedPlaceIds(@Param("userId") Long userId, @Param("placeIds") List<Long> placeIds);

    void deleteByUserId(Long userId);
}
