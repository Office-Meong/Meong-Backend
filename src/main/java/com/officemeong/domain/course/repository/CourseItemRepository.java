package com.officemeong.domain.course.repository;

import com.officemeong.domain.course.entity.CourseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseItemRepository extends JpaRepository<CourseItem, Long> {

    Optional<CourseItem> findByIdAndCourseId(Long id, Long courseId);
}
