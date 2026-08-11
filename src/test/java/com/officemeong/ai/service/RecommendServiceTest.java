package com.officemeong.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.ai.client.OpenAiClient;
import com.officemeong.ai.dto.PlaceRecommendResponse;
import com.officemeong.domain.dog.entity.Dog;
import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.favorite.entity.Favorite;
import com.officemeong.domain.favorite.repository.FavoriteRepository;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.enums.PlaceType;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendService 단위 테스트")
class RecommendServiceTest {

    @Mock PlaceRepository placeRepository;
    @Mock DogRepository dogRepository;
    @Mock FavoriteRepository favoriteRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock OpenAiClient openAiClient;
    @Spy ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks RecommendService recommendService;

    private Place mockPlace1, mockPlace2, mockPlace3;

    @BeforeEach
    void setUp() {
        mockPlace1 = mockPlace(10L, "카페A", PlaceType.FOOD, 80);
        mockPlace2 = mockPlace(20L, "숙소B", PlaceType.STAY, 75);
        mockPlace3 = mockPlace(30L, "코워킹C", PlaceType.WORK_PLACE, 70);

        lenient().when(placeRepository.findIdsByFilterOrderByScore(eq(Region.GANGNEUNG), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(10L, 20L, 30L), PageRequest.of(0, 20), 3));
        lenient().when(placeRepository.findByIdsWithSummaryDetails(List.of(10L, 20L, 30L)))
                .thenReturn(List.of(mockPlace1, mockPlace2, mockPlace3));
        lenient().when(favoriteRepository.findByUserIdWithPlace(anyLong())).thenReturn(List.of());
        lenient().when(reviewRepository.findByUserIdWithPlace(anyLong())).thenReturn(List.of());
    }

    @Test
    @DisplayName("Claude 성공 - 3개 추천 반환")
    void recommend_Claude성공_3개반환() {
        String claudeJson = """
                {"recommendations":[
                    {"placeId":10,"reason":"카페A 추천 이유"},
                    {"placeId":20,"reason":"숙소B 추천 이유"},
                    {"placeId":30,"reason":"코워킹C 추천 이유"}
                ]}""";
        when(openAiClient.call(any(), any())).thenReturn(claudeJson);

        List<PlaceRecommendResponse> result = recommendService.recommend(Region.GANGNEUNG, null, 1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPlaceId()).isEqualTo(10L);
        assertThat(result.get(0).getReason()).isEqualTo("카페A 추천 이유");
    }

    @Test
    @DisplayName("Claude 실패 - 점수 기반 폴백 3개 반환")
    void recommend_Claude실패_폴백() {
        when(openAiClient.call(any(), any())).thenThrow(new RuntimeException("API 오류"));

        List<PlaceRecommendResponse> result = recommendService.recommend(Region.GANGNEUNG, null, 1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getReason()).isEqualTo("펫워크 지수 기반 추천 장소입니다.");
    }

    @Test
    @DisplayName("후보 장소 없을 때 빈 리스트 반환")
    void recommend_후보없음_빈리스트() {
        when(placeRepository.findIdsByFilterOrderByScore(eq(Region.WONJU), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        List<PlaceRecommendResponse> result = recommendService.recommend(Region.WONJU, null, 1L);

        assertThat(result).isEmpty();
        verifyNoInteractions(openAiClient);
    }

    @Test
    @DisplayName("dogId 지정 - 반려견 정보 포함해 Claude 호출")
    void recommend_dogId포함_성공() {
        Dog dog = mock(Dog.class);
        lenient().when(dog.getName()).thenReturn("콩이");
        lenient().when(dog.getBreed()).thenReturn("말티즈");
        lenient().when(dog.getWeightKg()).thenReturn(BigDecimal.valueOf(5.0));
        lenient().when(dog.getBirthDate()).thenReturn(null);
        lenient().when(dog.getIsNeutered()).thenReturn(true);
        when(dogRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(dog));

        String claudeJson = """
                {"recommendations":[
                    {"placeId":10,"reason":"콩이에게 적합"},
                    {"placeId":20,"reason":"숙소 추천"},
                    {"placeId":30,"reason":"업무 공간"}
                ]}""";
        when(openAiClient.call(any(), any())).thenReturn(claudeJson);

        List<PlaceRecommendResponse> result = recommendService.recommend(Region.GANGNEUNG, 7L, 1L);

        assertThat(result).hasSize(3);
        verify(openAiClient).call(any(), contains("콩이"));
    }

    @Test
    @DisplayName("존재하지 않는 반려견 ID - NoSuchElementException 발생")
    void recommend_없는반려견_예외() {
        when(dogRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recommendService.recommend(Region.GANGNEUNG, 99L, 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Claude 마크다운 코드블록 응답 - 정상 파싱")
    void recommend_마크다운_코드블록_파싱() {
        String claudeMarkdown = """
                ```json
                {"recommendations":[
                    {"placeId":10,"reason":"이유1"},
                    {"placeId":20,"reason":"이유2"},
                    {"placeId":30,"reason":"이유3"}
                ]}
                ```""";
        when(openAiClient.call(any(), any())).thenReturn(claudeMarkdown);

        List<PlaceRecommendResponse> result = recommendService.recommend(Region.GANGNEUNG, null, 1L);

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("즐겨찾기·리뷰 이력 있을 때 프롬프트에 포함")
    void recommend_사용자_컨텍스트_포함() {
        Place favPlace = mockPlace(100L, "즐겨찾기장소", PlaceType.TOUR, 65);
        Favorite fav = mock(Favorite.class);
        lenient().when(fav.getPlace()).thenReturn(favPlace);
        when(favoriteRepository.findByUserIdWithPlace(1L)).thenReturn(List.of(fav));

        String claudeJson = """
                {"recommendations":[
                    {"placeId":10,"reason":"이유1"},
                    {"placeId":20,"reason":"이유2"},
                    {"placeId":30,"reason":"이유3"}
                ]}""";
        when(openAiClient.call(any(), contains("즐겨찾기장소"))).thenReturn(claudeJson);

        List<PlaceRecommendResponse> result = recommendService.recommend(Region.GANGNEUNG, null, 1L);

        assertThat(result).hasSize(3);
    }

    // ──── 헬퍼 ────

    private Place mockPlace(Long id, String name, PlaceType type, int score) {
        Place p = mock(Place.class);
        lenient().when(p.getId()).thenReturn(id);
        lenient().when(p.getName()).thenReturn(name);
        lenient().when(p.getPlaceType()).thenReturn(type);
        lenient().when(p.getRegion()).thenReturn(Region.GANGNEUNG);
        lenient().when(p.getAddress()).thenReturn("강원도 강릉시");
        lenient().when(p.getScore()).thenReturn(null);
        lenient().when(p.getPetCondition()).thenReturn(null);
        return p;
    }
}
