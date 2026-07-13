package com.officemeong.domain.favorite.repository;

import com.officemeong.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.place WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdWithPlace(@Param("userId") Long userId);

    Optional<Favorite> findByUserIdAndPlaceId(Long userId, Long placeId);

    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);
}
