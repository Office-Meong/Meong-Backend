package com.officemeong.domain.course.repository;

import com.officemeong.domain.course.entity.CourseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseItemRepository extends JpaRepository<CourseItem, Long> {

    Optional<CourseItem> findByIdAndCourseId(Long id, Long courseId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CourseItem ci WHERE ci.course.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
