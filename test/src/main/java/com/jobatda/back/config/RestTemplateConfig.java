package com.jobatda.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplateConfig 클래스
 * HTTP 클라이언트 라이브러리인 RestTemplate을 설정하는 Configuration 클래스
 * 
 * RestTemplate란?
 * - Spring에서 제공하는 HTTP 클라이언트
 * - REST API 호출을 위한 동기식 클라이언트
 * - 외부 API 호출 시 사용 (네이버 API, OpenAI API 등)
 * 
 * 이 설정의 목적:
 * - RestTemplate을 Spring Bean으로 등록하여 의존성 주입으로 사용 가능하게 함
 * - 다른 서비스들(AiImageService, NaverNewsService 등)에서 주입받아 사용
 * 
 * 향후 개선점:
 * - 타임아웃 설정 추가 가능
 * - 커넥션 풀 설정 가능
 * - 에러 핸들러 설정 가능
 * - Spring WebFlux의 WebClient로 대체 고려 (비동기 지원)
 */
@Configuration  // Spring의 설정 클래스임을 표시
public class RestTemplateConfig {

    /**
     * RestTemplate Bean을 생성하여 Spring 컨테이너에 등록
     * 
     * @return 기본 설정으로 구성된 RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}