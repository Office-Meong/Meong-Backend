package com.officemeong.domain.review.repository;

import com.officemeong.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.place.id = :placeId ORDER BY r.createdAt DESC")
    List<Review> findByPlaceIdWithUser(@Param("placeId") Long placeId);

    Optional<Review> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);
}
