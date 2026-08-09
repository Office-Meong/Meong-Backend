package com.officemeong.domain.course.repository;

import com.officemeong.domain.course.entity.CourseChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseChecklistItemRepository extends JpaRepository<CourseChecklistItem, Long> {

    List<CourseChecklistItem> findByCourseIdOrderByDisplayOrderAsc(Long courseId);

    int countByCourseId(Long courseId);
}
