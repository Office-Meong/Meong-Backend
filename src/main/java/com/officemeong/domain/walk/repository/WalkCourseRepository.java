package com.officemeong.domain.walk.repository;

import com.officemeong.domain.walk.entity.WalkCourse;
import com.officemeong.domain.place.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalkCourseRepository extends JpaRepository<WalkCourse, Long> {

    Optional<WalkCourse> findBySourceId(String sourceId);

    List<WalkCourse> findByRegion(Region region);
}
