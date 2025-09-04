package com.jobatda.back.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * NaverApiProperties 클래스
 * 네이버 API 관련 설정값들을 관리하는 Configuration Properties 클래스
 * 
 * Spring Boot Configuration Properties 패턴:
 * - application.yml에서 naver.api.* 로 시작하는 설정값들을 자동으로 매핑
 * - 타입 안전한 설정 관리 (IDE 자동완성 및 검증 지원)
 * - 설정값 변경 시 코드 수정 없이 yml 파일만 수정하면 됨
 * 
 * 설정 예시 (application.yml):
 * naver:
 *   api:
 *     client-id: your-client-id
 *     client-secret: your-client-secret
 *     news:
 *       url: https://openapi.naver.com/v1/search/news.json
 */
@Data                                              // Lombok: getter, setter, toString 등 자동 생성
@Component                                         // Spring Bean으로 등록
@ConfigurationProperties(prefix = "naver.api")     // application.yml의 naver.api 하위 설정들을 매핑
public class NaverApiProperties {
    
    /** 네이버 API 클라이언트 ID */
    private String clientId;
    
    /** 네이버 API 클라이언트 시크릿 */
    private String clientSecret;
    
    /** 뉴스 API 관련 설정 */
    private News news = new News();
    
    /**
     * 네이버 뉴스 API 관련 설정을 담는 내부 클래스
     * 
     * 중첩된 설정값들을 구조화하여 관리
     * (예: naver.api.news.url)
     */
    @Data
    public static class News {
        /** 네이버 뉴스 검색 API 엔드포인트 URL */
        private String url;
    }
}