package com.jobatda.back.domain.ai.controller;

// AI 이미지 생성 서비스
import com.jobatda.back.domain.ai.service.AiImageService;

// Lombok - 코드 간소화
import lombok.RequiredArgsConstructor;  // 생성자 주입
import lombok.extern.slf4j.Slf4j;       // 로깅

// Spring Framework
import org.springframework.http.ResponseEntity;  // HTTP 응답 엔티티 (상태코드 + 바디)
import org.springframework.web.bind.annotation.*; // 웹 MVC 어노테이션들

// Java 기본 라이브러리
import java.util.Map; // Key-Value 쌍으로 JSON 응답 구성

/**
 * AiImageController 클래스
 * AI 이미지 생성 관련 REST API를 제공하는 컨트롤러
 * 
 * 주요 기능:
 * - 텍스트 프롬프트를 받아서 AI 이미지 생성
 * - 생성된 이미지 URL 반환
 * - 에러 처리 및 상태 코드 관리
 * 
 * 외부 AI 서비스 연동 예시:
 * - OpenAI DALL-E, Stable Diffusion, Midjourney 등
 * - 비동기 처리나 큐 시스템 활용 가능
 */
@Slf4j                          // 로깅 기능 추가
@RestController                 // JSON 응답 REST API 컨트롤러
@RequestMapping("/api/ai")      // 기본 URL 매핑
@RequiredArgsConstructor       // final 필드 생성자 주입
public class AiImageController {

    /**
     * AI 이미지 생성 비즈니스 로직을 처리하는 서비스
     * 실제 AI API 호출이나 이미지 처리 로직 담당
     */
    private final AiImageService aiImageService;

    /**
     * AI 이미지 생성 API 엔드포인트
     * 
     * HTTP POST /api/ai/generate-image
     * 
     * @param request 이미지 생성 요청 데이터 (JSON 맵 형태)
     *                - "prompt": 이미지 생성을 위한 텍스트 설명
     * @return ResponseEntity<Map<String, String>> HTTP 상태코드 + JSON 응답
     * 
     * ResponseEntity 사용 이유:
     * - HTTP 상태 코드를 명시적으로 제어 가능 (200, 400, 500 등)
     * - 에러 상황에 따른 적절한 응답 제공
     * - RESTful API 설계 원칙 준수
     */
    @PostMapping("/generate-image")
    public ResponseEntity<Map<String, String>> generateImage(@RequestBody Map<String, String> request) {
        try {
            // 요청에서 프롬프트 추출
            String prompt = request.get("prompt");
            
            // 입력 검증: 프롬프트가 비어있는지 확인
            if (prompt == null || prompt.trim().isEmpty()) {
                // 400 Bad Request 응답
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "프롬프트는 필수입니다."));
            }

            log.info("AI 이미지 생성 요청: {}", prompt);
            
            // AI 서비스를 통한 이미지 생성 (실제 AI API 호출)
            String imageUrl = aiImageService.generateImage(prompt.trim());

            // 200 OK 응답 + 생성 결과 반환
            return ResponseEntity.ok(Map.of(
                    "success", "true",      // 성공 여부
                    "imageUrl", imageUrl,   // 생성된 이미지 URL
                    "prompt", prompt        // 사용된 프롬프트 (확인용)
            ));

        } catch (Exception e) {
            // 예외 발생시 로깅 및 에러 응답
            log.error("AI 이미지 생성 실패: {}", e.getMessage(), e);
            
            // 500 Internal Server Error 응답
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "이미지 생성 중 오류가 발생했습니다."));
        }
    }
}