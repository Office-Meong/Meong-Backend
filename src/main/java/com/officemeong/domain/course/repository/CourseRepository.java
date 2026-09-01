package com.officemeong.domain.course.repository;

import com.officemeong.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // i.place.images는 별도 컬렉션(bag) fetch라 같은 쿼리에서 함께 JOIN FETCH하면
    // MultipleBagFetchException이 발생하므로 제외 — hibernate.default_batch_fetch_size 설정으로
    // 지연 로딩 시 자동 배치 조회되어 N+1 문제는 발생하지 않음.
    @Query("SELECT c FROM Course c JOIN FETCH c.items i JOIN FETCH i.place p " +
           "LEFT JOIN FETCH p.operation WHERE c.id = :id AND c.user.id = :userId")
    Optional<Course> findByIdAndUserIdWithItems(@Param("id") Long id, @Param("userId") Long userId);

    Optional<Course> findByIdAndUserId(Long id, Long userId);

    List<Course> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}
