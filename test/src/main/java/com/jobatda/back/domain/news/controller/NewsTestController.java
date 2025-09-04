package com.jobatda.back.domain.news.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/news/test")
@RequiredArgsConstructor
public class NewsTestController {

    @Value("${naver.api.client-id:NOT_SET}")
    private String clientId;
    
    @Value("${naver.api.client-secret:NOT_SET}")
    private String clientSecret;
    
    @Value("${naver.api.news.url:NOT_SET}")
    private String apiUrl;

    @GetMapping("/config")
    public Map<String, Object> testConfig() {
        log.info("API 설정 확인 요청");
        log.info("실제 Client ID 값: {}", clientId);
        log.info("실제 Client Secret 값: {}", clientSecret);
        
        return Map.of(
            "clientIdExists", clientId != null && !clientId.equals("NOT_SET") && !clientId.isEmpty(),
            "clientIdValue", clientId != null ? clientId : "null",
            "clientIdLength", clientId != null ? clientId.length() : 0,
            "clientSecretExists", clientSecret != null && !clientSecret.equals("NOT_SET") && !clientSecret.isEmpty(),
            "clientSecretValue", clientSecret != null ? clientSecret : "null",
            "clientSecretLength", clientSecret != null ? clientSecret.length() : 0,
            "apiUrl", apiUrl,
            "status", "API 설정 테스트 완료"
        );
    }
    
    @GetMapping("/simple-test")
    public Map<String, String> simpleTest() {
        return Map.of(
            "message", "테스트 성공",
            "clientId", clientId != null ? clientId : "null",
            "clientSecret", clientSecret != null ? clientSecret : "null"
        );
    }
}