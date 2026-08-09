package com.officemeong.domain.course.entity;

import com.officemeong.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "course_checklist_items",
    indexes = @Index(name = "idx_cci_course_id", columnList = "course_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseChecklistItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(name = "is_checked", nullable = false)
    private boolean checked;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    public CourseChecklistItem(Course course, String content, int displayOrder) {
        this.course = course;
        this.content = content;
        this.checked = false;
        this.displayOrder = displayOrder;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void toggleChecked(boolean checked) {
        this.checked = checked;
    }
}
