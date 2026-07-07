package com.officemeong.domain.dog.entity;

import com.officemeong.common.entity.BaseTimeEntity;
import com.officemeong.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "dogs",
    indexes = @Index(name = "idx_user_id", columnList = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String breed;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "is_neutered")
    private Boolean isNeutered = false;

    @Builder
    public Dog(User user, String name, String breed, BigDecimal weightKg,
               LocalDate birthDate, Boolean isNeutered) {
        this.user = user;
        this.name = name;
        this.breed = breed;
        this.weightKg = weightKg;
        this.birthDate = birthDate;
        this.isNeutered = isNeutered != null ? isNeutered : false;
    }

    public void update(String name, String breed, BigDecimal weightKg,
                       LocalDate birthDate, Boolean isNeutered) {
        this.name = name;
        this.breed = breed;
        this.weightKg = weightKg;
        this.birthDate = birthDate;
        this.isNeutered = isNeutered != null ? isNeutered : false;
    }
}
