package com.officemeong.common.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    // 외부 공공데이터 API가 응답 없이 멈추는 경우, 배치 전체가 무한정 걸려있지 않도록
    // 연결/응답 타임아웃을 명시적으로 둔다 (기본값 없으면 OS 수준 타임아웃까지 수십 분 걸릴 수 있음)
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int RESPONSE_TIMEOUT_SEC = 30;

    private HttpClient timeoutHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .responseTimeout(java.time.Duration.ofSeconds(RESPONSE_TIMEOUT_SEC))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS)));
    }

    @Bean
    public WebClient ktoWebClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(timeoutHttpClient()))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    public WebClient gwtoWebClient() {
        return WebClient.builder()
                .baseUrl("https://www.pettravel.kr/api")
                .clientConnector(new ReactorClientHttpConnector(timeoutHttpClient()))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }
}
