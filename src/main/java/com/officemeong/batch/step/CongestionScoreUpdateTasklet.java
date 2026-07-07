package com.officemeong.batch.step;

import com.officemeong.domain.congestion.repository.CongestionForecastRepository;
import com.officemeong.domain.place.entity.PlaceScore;
import com.officemeong.domain.place.enums.Region;
import com.officemeong.domain.place.repository.PlaceScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;

@Slf4j
@Component
@RequiredArgsConstructor
public class CongestionScoreUpdateTasklet implements Tasklet {

    private final CongestionForecastRepository forecastRepository;
    private final PlaceScoreRepository scoreRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate today = LocalDate.now();
        int updated = 0;

        for (Region region : Region.values()) {
            OptionalDouble avgRate = forecastRepository.findAvgRateByRegionAndDate(region, today);
            int score = avgRate.isPresent() ? mapRateToScore(avgRate.getAsDouble()) : 8;

            List<PlaceScore> scores = scoreRepository.findByPlaceRegion(region);
            for (PlaceScore ps : scores) {
                ps.updateCongestionScore(score);
            }
            scoreRepository.saveAll(scores);
            updated += scores.size();

            log.info("혼잡도 점수 업데이트: {} - 평균 {}% → {}점, {}개 장소",
                    region,
                    avgRate.isPresent() ? String.format("%.1f", avgRate.getAsDouble()) : "없음",
                    score, scores.size());
        }

        log.info("혼잡도 점수 업데이트 완료 - 총 {}개", updated);
        return RepeatStatus.FINISHED;
    }

    /**
     * 관광지집중률(%) → 혼잡도 점수 (0~15)
     * 실측 범위: 19.8~83%
     */
    private int mapRateToScore(double rate) {
        if (rate < 30) return 15;
        if (rate < 45) return 12;
        if (rate < 55) return 9;
        if (rate < 65) return 6;
        if (rate < 75) return 3;
        return 1;
    }
}
