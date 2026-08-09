package com.officemeong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OfficeMeongApplication {

    public static void main(String[] args) {
        // GWTO(pettravel.kr) 등 일부 외부 API 서버가 TLS 중간 인증서를 응답에 포함하지 않아,
        // JDK가 AIA(Authority Information Access) 확장을 통해 직접 보완하도록 설정
        System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
        SpringApplication.run(OfficeMeongApplication.class, args);
    }
}
