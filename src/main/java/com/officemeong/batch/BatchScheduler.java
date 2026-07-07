package com.officemeong.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("placeCollectJob")
    private final Job placeCollectJob;

    @Qualifier("congestionBatchJob")
    private final Job congestionBatchJob;

    @Qualifier("durunubiCollectJob")
    private final Job durunubiCollectJob;

    // 매주 월요일 새벽 3시 — 장소 수집
    @Scheduled(cron = "0 0 3 * * MON")
    public void runPlaceCollect() {
        run(placeCollectJob, "placeCollect");
    }

    // 매일 새벽 2시 — 관광지 혼잡도
    @Scheduled(cron = "0 0 2 * * *")
    public void runCongestionBatch() {
        run(congestionBatchJob, "congestion");
    }

    // 매월 1일 새벽 3시 30분 — 두루누비 코스
    @Scheduled(cron = "0 30 3 1 * *")
    public void runDurunubiCollect() {
        run(durunubiCollectJob, "durunubi");
    }

    private void run(Job job, String name) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.at", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, params);
            log.info("배치 실행 완료: {}", name);
        } catch (Exception e) {
            log.error("배치 실행 실패: {}", name, e);
        }
    }
}
