package com.officemeong.batch;

import com.officemeong.batch.step.DurunubiCollectTasklet;
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
public class DurunubiCollectJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DurunubiCollectTasklet durunubiCollectTasklet;

    @Bean
    public Job durunubiCollectJob() {
        return new JobBuilder("durunubiCollectJob", jobRepository)
                .start(durunubiCollectStep())
                .build();
    }

    @Bean
    public Step durunubiCollectStep() {
        return new StepBuilder("durunubiCollectStep", jobRepository)
                .tasklet(durunubiCollectTasklet, transactionManager)
                .build();
    }
}
