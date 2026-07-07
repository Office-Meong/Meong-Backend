package com.officemeong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OfficeMeongApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfficeMeongApplication.class, args);
    }
}
