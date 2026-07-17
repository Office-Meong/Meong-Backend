package com.officemeong.domain.dog.service;

import com.officemeong.domain.dog.dto.DogRequest;
import com.officemeong.domain.dog.dto.DogResponse;
import com.officemeong.domain.dog.entity.Dog;
import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DogService {

    private final DogRepository dogRepository;
    private final UserRepository userRepository;

    public List<DogResponse> getMyDogs(Long userId) {
        return dogRepository.findByUserId(userId).stream()
                .map(DogResponse::from)
                .toList();
    }

    @Transactional
    public DogResponse addDog(Long userId, DogRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));

        Dog dog = Dog.builder()
                .user(user)
                .name(request.getName())
                .breed(request.getBreed())
                .weightKg(request.getWeightKg())
                .birthDate(request.getBirthDate())
                .isNeutered(request.getIsNeutered())
                .imageUrl(request.getImageUrl())
                .sizeCategory(request.getSizeCategory())
                .activityLevel(request.getActivityLevel())
                .sociability(request.getSociability())
                .healthStatus(request.getHealthStatus())
                .build();

        return DogResponse.from(dogRepository.save(dog));
    }

    @Transactional
    public DogResponse updateDog(Long userId, Long dogId, DogRequest request) {
        Dog dog = dogRepository.findByIdAndUserId(dogId, userId)
                .orElseThrow(() -> new NoSuchElementException("반려견을 찾을 수 없습니다: " + dogId));

        dog.update(request.getName(), request.getBreed(), request.getWeightKg(),
                request.getBirthDate(), request.getIsNeutered(),
                request.getImageUrl(), request.getSizeCategory(), request.getActivityLevel(),
                request.getSociability(), request.getHealthStatus());

        return DogResponse.from(dog);
    }

    @Transactional
    public void deleteDog(Long userId, Long dogId) {
        Dog dog = dogRepository.findByIdAndUserId(dogId, userId)
                .orElseThrow(() -> new NoSuchElementException("반려견을 찾을 수 없습니다: " + dogId));
        dogRepository.delete(dog);
    }
}
