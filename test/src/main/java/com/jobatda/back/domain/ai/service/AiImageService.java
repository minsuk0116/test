package com.jobatda.back.domain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 이미지 생성 서비스
 * 
 * 주요 기능:
 * - OpenAI DALL-E API를 활용한 AI 이미지 생성
 * - 한국어 프롬프트 자동 번역 (네이버 파파고 API 활용)
 * - 데모 모드 지원 (API 키 미설정 시 샘플 이미지 제공)
 * - 예외 상황 처리 및 안정적인 서비스 제공
 * 
 * 프롬프트 처리 과정:
 * 1. 한국어 프롬프트 입력
 * 2. 네이버 파파고로 영어 번역
 * 3. OpenAI DALL-E API로 이미지 생성
 * 4. 생성된 이미지 URL 반환
 * 
 * 오류 처리:
 * - API 키 미설정 시 데모 이미지 제공
 * - 번역/생성 실패 시 대체 이미지 제공
 */
@Slf4j                           // 로깅 기능 자동 추가
@Service                         // Spring Service 컴포넌트
@RequiredArgsConstructor         // final 필드에 대한 의존성 주입 생성자
public class AiImageService {

    // === OpenAI API 설정 (application.yml에서 주입) ===
    
    /** OpenAI API 키 (기본값: demo-key) */
    @Value("${openai.api.key:demo-key}")
    private String openaiApiKey;

    /** OpenAI API 엔드포인트 URL */
    @Value("${openai.api.url:https://api.openai.com/v1/images/generations}")
    private String openaiApiUrl;

    /** 사용할 OpenAI 모델 (DALL-E 3) */
    @Value("${openai.api.model:dall-e-3}")
    private String openaiModel;

    /** 생성할 이미지 크기 */
    @Value("${openai.api.size:1024x1024}")
    private String openaiSize;

    /** 이미지 품질 설정 */
    @Value("${openai.api.quality:standard}")
    private String openaiQuality;

    /** HTTP 요청을 위한 RestTemplate */
    private final RestTemplate restTemplate;

    /**
     * AI 이미지 생성 메인 메서드
     * 
     * @param prompt 이미지 생성을 위한 텍스트 프롬프트 (한국어 지원)
     * @return 생성된 이미지의 URL
     * 
     * 처리 과정:
     * 1. API 키 확인 (demo-key인 경우 데모 모드)
     * 2. 프롬프트 영어 번역 (한국어 -> 영어)
     * 3. OpenAI DALL-E API 호출
     * 4. 응답 파싱 및 이미지 URL 추출
     * 5. 오류 시 데모 이미지 URL 반환
     */
    public String generateImage(String prompt) {
        try {
            log.info("AI 이미지 생성 요청: {}", prompt);

            // OpenAI API 키가 설정되지 않은 경우 데모 이미지 반환
            if ("demo-key".equals(openaiApiKey)) {
                log.info("데모 모드: 샘플 이미지 URL 반환");
                return generateDemoImageUrl(prompt);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            String finalPrompt = createEnglishPrompt(prompt);
            log.info("OpenAI API로 전송할 최종 프롬프트: {}", finalPrompt);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openaiModel);
            requestBody.put("prompt", finalPrompt);
            requestBody.put("n", 1);
            requestBody.put("size", openaiSize);
            requestBody.put("quality", openaiQuality);

            log.info("OpenAI API 요청 본문: {}", requestBody);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    openaiApiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("data") && responseBody.get("data") instanceof java.util.List) {
                    java.util.List<?> dataList = (java.util.List<?>) responseBody.get("data");
                    if (!dataList.isEmpty() && dataList.get(0) instanceof Map) {
                        Map<String, Object> imageData = (Map<String, Object>) dataList.get(0);
                        String imageUrl = (String) imageData.get("url");
                        log.info("AI 이미지 생성 성공: {}", imageUrl);
                        return imageUrl;
                    }
                }
            }

            log.warn("AI 이미지 생성 실패, 데모 이미지 반환");
            return generateDemoImageUrl(prompt);

        } catch (Exception e) {
            log.error("AI 이미지 생성 중 오류 발생: {}", e.getMessage(), e);
            return generateDemoImageUrl(prompt);
        }
    }

    private String createEnglishPrompt(String userPrompt) {
        log.info("원본 프롬프트: {}", userPrompt);
        
        // 이미 영어로 스타일이 적용된 프롬프트인지 확인 (모달에서 온 경우)
        if (userPrompt.contains("photorealistic") || userPrompt.contains("cartoon style") || 
            userPrompt.contains("artistic") || userPrompt.contains("watercolor") || 
            userPrompt.contains("oil painting") || userPrompt.contains("digital art")) {
            log.info("스타일 적용된 프롬프트 사용: {}", userPrompt);
            return userPrompt; // 이미 스타일이 적용된 경우 그대로 반환
        }
        
        // 한국어를 영어로 변환
        String englishPrompt = translateKoreanToEnglish(userPrompt);
        log.info("번역된 프롬프트: {}", englishPrompt);
        
        return englishPrompt;
    }
    
    private String translateKoreanToEnglish(String koreanText) {
        try {
            // 네이버 파파고 번역 API 사용 (이미 네이버 API 키가 있으므로)
            return translateWithNaverPapago(koreanText);
        } catch (Exception e) {
            log.warn("파파고 번역 실패, 기본 매핑 사용: {}", e.getMessage());
            // 파파고 번역 실패 시 기본 매핑 방식 사용
            return translateWithBasicMapping(koreanText);
        }
    }
    
    private String translateWithNaverPapago(String koreanText) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-Naver-Client-Id", "TZNSREKPWIKuqHcDfmvU");
            headers.set("X-Naver-Client-Secret", "OJmvBaDy3o");

            String requestBody = "source=ko&target=en&text=" + 
                URLEncoder.encode(koreanText, "UTF-8");

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/papago/n2mt",
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) responseBody.get("message");
                    if (message.containsKey("result")) {
                        Map<String, Object> result = (Map<String, Object>) message.get("result");
                        String translatedText = (String) result.get("translatedText");
                        log.info("파파고 번역 성공: {} -> {}", koreanText, translatedText);
                        return translatedText + ", high quality, detailed, professional artwork";
                    }
                }
            }

            throw new RuntimeException("파파고 API 응답 파싱 실패");

        } catch (UnsupportedEncodingException e) {
            log.error("URL 인코딩 실패: {}", e.getMessage());
            throw new RuntimeException("URL 인코딩 실패", e);
        } catch (Exception e) {
            log.error("파파고 번역 실패: {}", e.getMessage());
            throw e;
        }
    }
    
    private String translateWithBasicMapping(String koreanText) {
        // 기본 매핑 방식 (기존 코드)
        String translated = koreanText
            .replace("고양이", "cat")
            .replace("강아지", "dog")
            .replace("개", "dog")
            .replace("바다", "ocean")
            .replace("산", "mountain")
            .replace("하늘", "sky")
            .replace("꽃", "flower")
            .replace("나무", "tree")
            .replace("집", "house")
            .replace("자동차", "car")
            .replace("음식", "food")
            .replace("사람", "person")
            .replace("아름다운", "beautiful")
            .replace("예쁜", "pretty")
            .replace("큰", "big")
            .replace("작은", "small")
            .replace("빨간", "red")
            .replace("파란", "blue")
            .replace("노란", "yellow")
            .replace("초록", "green")
            .replace("아침", "morning")
            .replace("저녁", "evening")
            .replace("밤", "night")
            .replace("공원", "park")
            .replace("도시", "city")
            .replace("여행", "travel");
            
        // 번역되지 않은 한글이 있다면 기본 설명 추가
        if (translated.matches(".*[가-힣]+.*")) {
            translated = "A beautiful scene depicting: " + translated;
        }
        
        return translated + ", high quality, detailed, professional artwork";
    }

    private String generateDemoImageUrl(String prompt) {
        // 데모용 이미지 URL들 (무료 이미지 서비스 사용)
        String[] demoImages = {
                "https://picsum.photos/800/600?random=1",
                "https://picsum.photos/800/600?random=2", 
                "https://picsum.photos/800/600?random=3",
                "https://picsum.photos/800/600?random=4",
                "https://picsum.photos/800/600?random=5"
        };
        
        // 프롬프트 해시를 기반으로 일관된 이미지 선택
        int index = Math.abs(prompt.hashCode()) % demoImages.length;
        return demoImages[index];
    }
}