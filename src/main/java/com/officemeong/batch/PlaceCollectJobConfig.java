package com.officemeong.batch;

import com.officemeong.batch.step.GwtoPlaceCollectTasklet;
import com.officemeong.batch.step.KtoPlaceCollectTasklet;
import com.officemeong.batch.step.ScoreCalculateTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PlaceCollectJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final KtoPlaceCollectTasklet ktoPlaceCollectTasklet;
    private final GwtoPlaceCollectTasklet gwtoPlaceCollectTasklet;
    private final ScoreCalculateTasklet scoreCalculateTasklet;

    @Bean
    public Job placeCollectJob() {
        return new JobBuilder("placeCollectJob", jobRepository)
                .start(ktoCollectStep())
                .next(gwtoCollectStep())
                .next(scoreCalculateStep())
                .build();
    }

    @Bean
    public Step ktoCollectStep() {
        return new StepBuilder("ktoCollectStep", jobRepository)
                .tasklet(ktoPlaceCollectTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step gwtoCollectStep() {
        return new StepBuilder("gwtoCollectStep", jobRepository)
                .tasklet(gwtoPlaceCollectTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step scoreCalculateStep() {
        return new StepBuilder("scoreCalculateStep", jobRepository)
                .tasklet(scoreCalculateTasklet, transactionManager)
                .build();
    }
}
