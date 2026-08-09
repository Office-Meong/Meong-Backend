package com.officemeong.domain.course.repository;

import com.officemeong.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c JOIN FETCH c.items i JOIN FETCH i.place WHERE c.id = :id AND c.user.id = :userId")
    Optional<Course> findByIdAndUserIdWithItems(@Param("id") Long id, @Param("userId") Long userId);

    Optional<Course> findByIdAndUserId(Long id, Long userId);

    List<Course> findByUserIdOrderByCreatedAtDesc(Long userId);
}
