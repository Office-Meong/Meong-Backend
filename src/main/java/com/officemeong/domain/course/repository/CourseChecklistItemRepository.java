package com.officemeong.domain.course.repository;

import com.officemeong.domain.course.entity.CourseChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseChecklistItemRepository extends JpaRepository<CourseChecklistItem, Long> {

    List<CourseChecklistItem> findByCourseIdOrderByDisplayOrderAsc(Long courseId);

    int countByCourseId(Long courseId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CourseChecklistItem c WHERE c.course.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
