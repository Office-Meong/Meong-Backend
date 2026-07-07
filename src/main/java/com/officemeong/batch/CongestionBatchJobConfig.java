package com.officemeong.batch;

import com.officemeong.batch.step.CongestionCollectTasklet;
import com.officemeong.batch.step.CongestionScoreUpdateTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CongestionBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CongestionCollectTasklet congestionCollectTasklet;
    private final CongestionScoreUpdateTasklet congestionScoreUpdateTasklet;

    @Bean
    public Job congestionBatchJob() {
        return new JobBuilder("congestionBatchJob", jobRepository)
                .start(congestionCollectStep())
                .next(congestionScoreUpdateStep())
                .build();
    }

    @Bean
    public Step congestionCollectStep() {
        return new StepBuilder("congestionCollectStep", jobRepository)
                .tasklet(congestionCollectTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step congestionScoreUpdateStep() {
        return new StepBuilder("congestionScoreUpdateStep", jobRepository)
                .tasklet(congestionScoreUpdateTasklet, transactionManager)
                .build();
    }
}
