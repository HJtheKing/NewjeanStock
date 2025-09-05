package com.sascom.chickenstock.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

@Configuration
public class HttpClientConfig {

    @Value("${http.timeout.connect-ms:1500}")
    private int connectTimeoutMs;

    @Value("${http.timeout.read-ms:3000}")
    private int readTimeoutMs;

    @Value("${security.shared-secret}")
    private String sharedSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public RestTemplate restTemplate() {
        // 타임아웃/커넥션 설정
        CloseableHttpClient httpClient = HttpClients.custom().build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        RestTemplate rt = new RestTemplate(factory);

        // (핵심) 모든 요청에 HMAC 헤더 자동 부착
        ClientHttpRequestInterceptor hmacInterceptor = (request, body, execution) -> {
            // JSON Content-Type 보장
            if (!request.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)) {
                request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            }

            // body는 RestTemplate이 변환한 실제 전송 바이트
            String bodyJson = new String(body, StandardCharsets.UTF_8);

            // HMAC 서명 헤더 부착 (HmacSigner는 이미 있다고 가정)
            HmacSigner.sign(request.getHeaders(), bodyJson, sharedSecret);

            // 트레이싱용 선택 헤더
            request.getHeaders().add("X-Request-Id", UUID.randomUUID().toString());

            return execution.execute(request, body);
        };

        rt.setInterceptors(Collections.singletonList(hmacInterceptor));
        return rt;
    }
}