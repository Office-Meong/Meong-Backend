package com.officemeong.domain.dog.service;

import com.officemeong.domain.dog.dto.DogRequest;
import com.officemeong.domain.dog.dto.DogResponse;
import com.officemeong.domain.dog.entity.Dog;
import com.officemeong.domain.dog.enums.ActivityLevel;
import com.officemeong.domain.dog.enums.DogSize;
import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.user.entity.User;
import com.officemeong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DogService 단위 테스트")
class DogServiceTest {

    @Mock DogRepository dogRepository;
    @Mock UserRepository userRepository;

    @InjectMocks DogService dogService;

    @Test
    @DisplayName("반려견 목록 조회")
    void getMyDogs_목록_반환() {
        Dog dog = mockDog(1L, "뽀삐");
        when(dogRepository.findByUserId(1L)).thenReturn(List.of(dog));

        List<DogResponse> result = dogService.getMyDogs(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("뽀삐");
    }

    @Test
    @DisplayName("반려견 목록 - 빈 결과")
    void getMyDogs_빈_결과() {
        when(dogRepository.findByUserId(1L)).thenReturn(List.of());

        List<DogResponse> result = dogService.getMyDogs(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("반려견 등록 성공")
    void addDog_등록_성공() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        DogRequest request = mockDogRequest("뽀삐", "말티즈", new BigDecimal("3.5"));
        Dog savedDog = mockDog(1L, "뽀삐");
        when(dogRepository.save(any(Dog.class))).thenReturn(savedDog);

        DogResponse response = dogService.addDog(1L, request);

        assertThat(response.getName()).isEqualTo("뽀삐");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 반려견 등록 시 예외")
    void addDog_없는_사용자_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        DogRequest request = mockDogRequest("뽀삐", null, null);

        assertThatThrownBy(() -> dogService.addDog(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("반려견 정보 수정 성공")
    void updateDog_수정_성공() {
        Dog dog = mockDog(1L, "뽀삐");
        when(dogRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(dog));

        DogRequest request = mockDogRequest("초코", "포메라니안", new BigDecimal("2.0"));
        when(request.getBirthDate()).thenReturn(LocalDate.of(2022, 1, 1));
        when(request.getIsNeutered()).thenReturn(true);
        when(request.getSizeCategory()).thenReturn(DogSize.SMALL);
        when(request.getActivityLevel()).thenReturn(ActivityLevel.MEDIUM);

        dogService.updateDog(1L, 1L, request);

        verify(dog).update("초코", "포메라니안", new BigDecimal("2.0"),
                LocalDate.of(2022, 1, 1), true,
                null, DogSize.SMALL, ActivityLevel.MEDIUM, null, null);
    }

    @Test
    @DisplayName("타인의 반려견 수정 시 예외")
    void updateDog_권한_없음_예외() {
        when(dogRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        DogRequest request = mockDogRequest("초코", null, null);

        assertThatThrownBy(() -> dogService.updateDog(2L, 1L, request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("반려견 삭제 성공")
    void deleteDog_삭제_성공() {
        Dog dog = mockDog(1L, "뽀삐");
        when(dogRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(dog));

        dogService.deleteDog(1L, 1L);

        verify(dogRepository).delete(dog);
    }

    @Test
    @DisplayName("타인의 반려견 삭제 시 예외")
    void deleteDog_권한_없음_예외() {
        when(dogRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dogService.deleteDog(2L, 1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    private Dog mockDog(Long id, String name) {
        Dog dog = mock(Dog.class);
        lenient().when(dog.getId()).thenReturn(id);
        lenient().when(dog.getName()).thenReturn(name);
        lenient().when(dog.getBreed()).thenReturn(null);
        lenient().when(dog.getWeightKg()).thenReturn(null);
        lenient().when(dog.getBirthDate()).thenReturn(null);
        lenient().when(dog.getIsNeutered()).thenReturn(false);
        lenient().when(dog.getImageUrl()).thenReturn(null);
        lenient().when(dog.getSizeCategory()).thenReturn(null);
        lenient().when(dog.getActivityLevel()).thenReturn(null);
        lenient().when(dog.getSociability()).thenReturn(null);
        lenient().when(dog.getHealthStatus()).thenReturn(null);
        return dog;
    }

    private DogRequest mockDogRequest(String name, String breed, BigDecimal weightKg) {
        DogRequest request = mock(DogRequest.class);
        lenient().when(request.getName()).thenReturn(name);
        lenient().when(request.getBreed()).thenReturn(breed);
        lenient().when(request.getWeightKg()).thenReturn(weightKg);
        lenient().when(request.getBirthDate()).thenReturn(null);
        lenient().when(request.getIsNeutered()).thenReturn(null);
        lenient().when(request.getImageUrl()).thenReturn(null);
        lenient().when(request.getSizeCategory()).thenReturn(null);
        lenient().when(request.getActivityLevel()).thenReturn(null);
        lenient().when(request.getSociability()).thenReturn(null);
        lenient().when(request.getHealthStatus()).thenReturn(null);
        return request;
    }
}
