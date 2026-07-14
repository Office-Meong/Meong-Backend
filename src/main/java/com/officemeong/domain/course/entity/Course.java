package com.officemeong.domain.course.entity;

import com.officemeong.common.entity.BaseTimeEntity;
import com.officemeong.domain.course.enums.WorkFocusLevel;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "courses",
    indexes = @Index(name = "idx_course_user_id", columnList = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Region region;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "work_start_time", nullable = false)
    private LocalTime workStartTime;

    @Column(name = "work_end_time", nullable = false)
    private LocalTime workEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_focus_level", nullable = false, length = 10)
    private WorkFocusLevel workFocusLevel;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC, visitOrder ASC")
    private List<CourseItem> items = new ArrayList<>();

    @Builder
    public Course(User user, Region region, LocalDate startDate, LocalDate endDate,
                  LocalTime workStartTime, LocalTime workEndTime, WorkFocusLevel workFocusLevel) {
        this.user = user;
        this.region = region;
        this.startDate = startDate;
        this.endDate = endDate;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.workFocusLevel = workFocusLevel;
    }

    public void addItem(CourseItem item) {
        items.add(item);
    }
}
