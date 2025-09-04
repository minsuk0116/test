package com.jobatda.back.config;

// Spring Framework Configuration 관련
import org.springframework.context.annotation.Bean;          // Bean 등록
import org.springframework.context.annotation.Configuration; // 설정 클래스 표시
import org.springframework.web.servlet.config.annotation.CorsRegistry;    // CORS 설정 등록
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // MVC 설정 인터페이스

/**
 * CorsConfig 클래스
 * CORS(Cross-Origin Resource Sharing) 정책을 설정하는 Configuration 클래스
 * 
 * CORS란?
 * - 웹 브라우저가 다른 도메인의 리소스에 접근할 때 적용되는 보안 정책
 * - 기본적으로 Same-Origin Policy에 의해 차단됨
 * - 백엔드 서버에서 명시적으로 허용해야 프론트엔드에서 API 호출 가능
 * 
 * 이 설정의 목적:
 * React 개발 서버(localhost:3000)에서 Spring Boot 서버(localhost:8080)로의
 * API 요청을 허용하기 위함
 */
@Configuration  // Spring의 설정 클래스임을 표시
public class CorsConfig {

    /**
     * CORS 정책을 설정하는 WebMvcConfigurer Bean 생성
     * 
     * @return WebMvcConfigurer 구현체
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")                                    // 모든 API 경로에 대해
                        .allowedOrigins("http://localhost:3000")              // React 개발 서버 허용
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                        .allowedHeaders("*")                                  // 모든 헤더 허용
                        .allowCredentials(true);                              // 인증 정보 포함 요청 허용
                        
                // 운영 환경에서는 보안을 위해 구체적인 도메인과 헤더만 허용하는 것이 좋음
            }
        };
    }
}
