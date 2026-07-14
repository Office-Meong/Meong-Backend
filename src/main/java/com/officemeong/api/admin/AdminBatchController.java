package com.officemeong.api.admin;

import com.officemeong.common.dto.ApiResponse;
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

    @PostMapping("/collect")
    public ResponseEntity<ApiResponse<String>> runPlaceCollect() {
        return runJob(placeCollectJob, "placeCollect");
    }

    @PostMapping("/congestion")
    public ResponseEntity<ApiResponse<String>> runCongestion() {
        return runJob(congestionBatchJob, "congestion");
    }

    @PostMapping("/durunubi")
    public ResponseEntity<ApiResponse<String>> runDurunubi() {
        return runJob(durunubiCollectJob, "durunubi");
    }

    @PostMapping("/score")
    public ResponseEntity<ApiResponse<String>> runScoreCalculate() {
        return runJob(scoreCalculateJob, "score");
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
