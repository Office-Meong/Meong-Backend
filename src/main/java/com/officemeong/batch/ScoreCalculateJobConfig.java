package com.officemeong.batch;

import com.officemeong.batch.step.ScoreCalculateTasklet;
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
public class ScoreCalculateJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ScoreCalculateTasklet scoreCalculateTasklet;

    @Bean
    public Job scoreCalculateJob() {
        return new JobBuilder("scoreCalculateJob", jobRepository)
                .start(scoreOnlyStep())
                .build();
    }

    @Bean
    public Step scoreOnlyStep() {
        return new StepBuilder("scoreOnlyStep", jobRepository)
                .tasklet(scoreCalculateTasklet, transactionManager)
                .build();
    }
}
