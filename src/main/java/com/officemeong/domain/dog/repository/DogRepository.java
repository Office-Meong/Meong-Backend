package com.officemeong.domain.dog.repository;

import com.officemeong.domain.dog.entity.Dog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DogRepository extends JpaRepository<Dog, Long> {

    List<Dog> findByUserId(Long userId);

    Optional<Dog> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}
