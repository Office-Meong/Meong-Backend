package com.officemeong.domain.dog.repository;

import com.officemeong.domain.dog.entity.Dog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DogRepository extends JpaRepository<Dog, Long> {

    List<Dog> findByUserId(Long userId);

    Optional<Dog> findByIdAndUserId(Long id, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Dog d WHERE d.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
