package com.officemeong.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemeong.ai.client.ClaudeClient;
import com.officemeong.ai.dto.PlaceRecommendResponse;
import com.officemeong.domain.dog.entity.Dog;
import com.officemeong.domain.dog.repository.DogRepository;
import com.officemeong.domain.favorite.entity.Favorite;
import com.officemeong.domain.favorite.repository.FavoriteRepository;
import com.officemeong.domain.place.entity.Place;
import com.officemeong.domain.place.entity.PlacePetCondition;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceRepository;
import com.officemeong.domain.review.entity.Review;
import com.officemeong.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendService {

    private static final int CANDIDATE_COUNT = 20;
    private static final int RECOMMEND_COUNT = 3;
    private static final int CONTEXT_LIMIT = 5;

    private final PlaceRepository placeRepository;
    private final DogRepository dogRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
    private final ClaudeClient claudeClient;
    private final ObjectMapper objectMapper;

    public List<PlaceRecommendResponse> recommend(Region region, Long dogId, Long userId) {
        // 1. 후보 장소 로드 (지역 점수 상위 20개)
        List<Long> candidateIds = placeRepository
                .findIdsByFilterOrderByScore(region, null, PageRequest.of(0, CANDIDATE_COUNT))
                .getContent();
        if (candidateIds.isEmpty()) {
            return List.of();
        }
        List<Place> candidates = placeRepository.findByIdsWithSummaryDetails(candidateIds);
        Map<Long, Place> candidateMap = candidates.stream()
                .collect(Collectors.toMap(Place::getId, p -> p));

        // 2. 반려견 정보 (선택)
        Dog dog = null;
        if (dogId != null) {
            dog = dogRepository.findByIdAndUserId(dogId, userId)
                    .orElseThrow(() -> new NoSuchElementException("반려견을 찾을 수 없습니다. id=" + dogId));
        }

        // 3. 사용자 맥락 로드
        List<Favorite> favorites = favoriteRepository.findByUserIdWithPlace(userId)
                .stream().limit(CONTEXT_LIMIT).toList();
        List<Review> reviews = reviewRepository.findByUserIdWithPlace(userId)
                .stream().limit(CONTEXT_LIMIT).toList();

        // 4. 프롬프트 생성 → Claude 호출 → 파싱
        try {
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(region, dog, favorites, reviews, candidates);
            String rawResponse = claudeClient.call(systemPrompt, userPrompt);
            String json = stripMarkdownCodeBlock(rawResponse);
            return parseRecommendations(json, candidateMap);
        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI 추천 실패, 점수 기반 폴백 적용: {}", e.getMessage());
            return fallback(candidates);
        }
    }

    private String buildSystemPrompt() {
        return """
                당신은 반려동물 동반 워케이션 코디네이터입니다.
                사용자와 반려견 정보, 후보 장소 목록을 보고 가장 적합한 장소 3곳을 추천하세요.

                출력 형식은 반드시 아래 JSON만 출력하세요 (마크다운 코드 블록 없이):
                {"recommendations":[{"placeId":숫자,"reason":"추천 이유(한국어, 2~3문장)"},{"placeId":숫자,"reason":"..."},{"placeId":숫자,"reason":"..."}]}

                규칙:
                - placeId는 반드시 후보 장소 목록에 있는 숫자만 사용하세요.
                - reason은 왜 이 반려견과 사용자에게 적합한지 구체적으로 설명하세요.
                - 숙박, 음식점, 관광지 등 다양한 유형을 포함하여 추천하세요.
                - 과거 즐겨찾기·리뷰를 참고해 취향에 맞는 장소를 선정하세요.
                """;
    }

    private String buildUserPrompt(Region region, Dog dog, List<Favorite> favorites,
                                   List<Review> reviews, List<Place> candidates) {
        StringBuilder sb = new StringBuilder();

        sb.append("## 반려견 정보\n");
        if (dog != null) {
            sb.append("- 이름: ").append(dog.getName()).append("\n");
            if (dog.getBreed() != null) sb.append("- 품종: ").append(dog.getBreed()).append("\n");
            if (dog.getWeightKg() != null) sb.append("- 체중: ").append(dog.getWeightKg()).append("kg\n");
            if (dog.getBirthDate() != null) {
                int age = Period.between(dog.getBirthDate(), LocalDate.now()).getYears();
                sb.append("- 나이: 약 ").append(age).append("살\n");
            }
            if (dog.getIsNeutered() != null) {
                sb.append("- 중성화: ").append(Boolean.TRUE.equals(dog.getIsNeutered()) ? "완료" : "미완료").append("\n");
            }
        } else {
            sb.append("- 반려견 정보 없음\n");
        }

        sb.append("\n## 과거 즐겨찾기\n");
        if (favorites.isEmpty()) {
            sb.append("- 즐겨찾기 이력 없음\n");
        } else {
            favorites.forEach(f -> sb.append("- ").append(f.getPlace().getName())
                    .append(" (").append(f.getPlace().getPlaceType()).append(")\n"));
        }

        sb.append("\n## 작성한 리뷰\n");
        if (reviews.isEmpty()) {
            sb.append("- 리뷰 이력 없음\n");
        } else {
            reviews.forEach(r -> sb.append("- ").append(r.getPlace().getName())
                    .append(" 별점").append(r.getScore()).append("점: ").append(r.getContent()).append("\n"));
        }

        sb.append("\n## 여행 지역\n- ").append(region.name()).append("\n");

        sb.append("\n## 추천 후보 장소 (점수 상위 ").append(candidates.size()).append("개)\n");
        for (int i = 0; i < candidates.size(); i++) {
            Place p = candidates.get(i);
            int score = p.getScore() != null ? p.getScore().getTotalScore() : 0;
            sb.append(i + 1).append(". [ID:").append(p.getId()).append("] ")
              .append(p.getName()).append(" (").append(p.getPlaceType()).append(", 점수:").append(score).append(")");

            PlacePetCondition pc = p.getPetCondition();
            if (pc != null) {
                if (pc.getAcmpyType() != null) sb.append(", 동반:").append(pc.getAcmpyType());
                if (Boolean.FALSE.equals(pc.getIsCageRequired())) sb.append(", 케이지불필요");
                if (pc.getPetWeightLimitKg() != null) sb.append(", 체중제한:").append(pc.getPetWeightLimitKg()).append("kg이하");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private List<PlaceRecommendResponse> parseRecommendations(String json, Map<Long, Place> candidateMap) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode recs = root.path("recommendations");

        List<PlaceRecommendResponse> result = new ArrayList<>();
        for (JsonNode rec : recs) {
            Long placeId = rec.path("placeId").asLong();
            String reason = rec.path("reason").asText();
            Place place = candidateMap.get(placeId);
            if (place != null) {
                result.add(PlaceRecommendResponse.of(place, reason));
            }
        }
        return result.stream().limit(RECOMMEND_COUNT).toList();
    }

    private List<PlaceRecommendResponse> fallback(List<Place> candidates) {
        return candidates.stream()
                .limit(RECOMMEND_COUNT)
                .map(p -> PlaceRecommendResponse.of(p, "펫워크 지수 기반 추천 장소입니다."))
                .toList();
    }

    private String stripMarkdownCodeBlock(String text) {
        String trimmed = text.strip();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastBacktick = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastBacktick > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastBacktick).strip();
            }
        }
        return trimmed;
    }
}
