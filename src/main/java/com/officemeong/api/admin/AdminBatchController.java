package com.officemeong.api.admin;

import com.officemeong.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 배치", description = "데이터 수집/계산 배치를 수동으로 즉시 실행하는 관리자용 API. " +
        "요청 헤더에 X-Admin-Key: {ADMIN_API_KEY} 를 포함해야 합니다.")
@SecurityRequirement(name = "AdminApiKey")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/batch")
@RequiredArgsConstructor
public class AdminBatchController {

    private final JobLauncher jobLauncher;

    @Qualifier("placeCollectJob")
    private final Job placeCollectJob;

    @Qualifier("congestionBatchJob")
    private final Job congestionBatchJob;

    @Qualifier("durunubiCollectJob")
    private final Job durunubiCollectJob;

    @Qualifier("scoreCalculateJob")
    private final Job scoreCalculateJob;

    @Qualifier("kakaoFoodSupplementJob")
    private final Job kakaoFoodSupplementJob;

    @Operation(summary = "장소 수집 배치 즉시 실행", description = "KTO/GWTO 장소 데이터를 수집하여 저장합니다. (평소 매주 월요일 새벽 3시 자동 실행)")
    @PostMapping("/collect")
    public ResponseEntity<ApiResponse<String>> runPlaceCollect() {
        return runJob(placeCollectJob, "placeCollect");
    }

    @Operation(summary = "혼잡도 수집 배치 즉시 실행", description = "관광지 혼잡도 예보 데이터를 수집합니다. (평소 매일 새벽 2시 자동 실행)")
    @PostMapping("/congestion")
    public ResponseEntity<ApiResponse<String>> runCongestion() {
        return runJob(congestionBatchJob, "congestion");
    }

    @Operation(summary = "두루누비 코스 수집 배치 즉시 실행", description = "두루누비 산책 코스 데이터를 수집합니다. (평소 매월 1일 새벽 3시 30분 자동 실행)")
    @PostMapping("/durunubi")
    public ResponseEntity<ApiResponse<String>> runDurunubi() {
        return runJob(durunubiCollectJob, "durunubi");
    }

    @Operation(summary = "펫-워크 지수 재계산 배치 즉시 실행", description = "장소별 펫-워크 지수(점수)를 다시 계산합니다.")
    @PostMapping("/score")
    public ResponseEntity<ApiResponse<String>> runScoreCalculate() {
        return runJob(scoreCalculateJob, "score");
    }

    @Operation(summary = "원주 식당 카카오 보강 배치 즉시 실행",
            description = "KTO/GWTO의 원주 식음료 데이터가 대부분 카페로 분류되어 순수 식당이 부족한 공백을 " +
                    "카카오 로컬 API(\"원주 애견동반 식당\" 키워드 검색)로 보강합니다. " +
                    "반려동물 동반 정책은 GWTO/KTO와 달리 공식 검증된 데이터가 아닙니다.")
    @PostMapping("/kakao-food")
    public ResponseEntity<ApiResponse<String>> runKakaoFoodSupplement() {
        return runJob(kakaoFoodSupplementJob, "kakaoFoodSupplement");
    }

    private ResponseEntity<ApiResponse<String>> runJob(Job job, String name) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.at", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, params);
            log.info("어드민 배치 실행: {}", name);
            return ResponseEntity.ok(ApiResponse.ok(name + " 배치 실행 완료"));
        } catch (Exception e) {
            log.error("어드민 배치 실패: {}", name, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }
}
