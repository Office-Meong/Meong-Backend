package com.officemeong.batch;

import com.officemeong.batch.step.KakaoFoodSupplementTasklet;
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
public class KakaoFoodSupplementJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final KakaoFoodSupplementTasklet kakaoFoodSupplementTasklet;

    @Bean
    public Job kakaoFoodSupplementJob() {
        return new JobBuilder("kakaoFoodSupplementJob", jobRepository)
                .start(kakaoFoodSupplementStep())
                .build();
    }

    @Bean
    public Step kakaoFoodSupplementStep() {
        return new StepBuilder("kakaoFoodSupplementStep", jobRepository)
                .tasklet(kakaoFoodSupplementTasklet, transactionManager)
                .build();
    }
}
