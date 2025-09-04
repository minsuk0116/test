package com.jobatda.back.domain.sentiment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisService {

    @Value("${openai.api.key:demo-key}")
    private String openaiApiKey;

    private final RestTemplate restTemplate;

    public SentimentResult analyzeSentiment(String title, String content) {
        try {
            log.info("감성분석 요청 - 제목: {}, 내용 길이: {}", title, content.length());

            // OpenAI API 키가 설정되지 않은 경우 데모 결과 반환
            if ("demo-key".equals(openaiApiKey)) {
                log.info("데모 모드: 샘플 감성분석 결과 반환");
                return generateDemoSentiment(title, content);
            }

            return analyzeWithOpenAI(title, content);

        } catch (Exception e) {
            log.error("감성분석 중 오류 발생: {}", e.getMessage(), e);
            return generateDemoSentiment(title, content);
        }
    }

    private SentimentResult analyzeWithOpenAI(String title, String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            String prompt = createSentimentPrompt(title, content);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 300);
            requestBody.put("temperature", 0.3);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("choices") && responseBody.get("choices") instanceof List) {
                    List<?> choices = (List<?>) responseBody.get("choices");
                    if (!choices.isEmpty() && choices.get(0) instanceof Map) {
                        Map<String, Object> choice = (Map<String, Object>) choices.get(0);
                        if (choice.containsKey("message")) {
                            Map<String, Object> message = (Map<String, Object>) choice.get("message");
                            String analysisResult = (String) message.get("content");
                            
                            log.info("OpenAI 감성분석 결과: {}", analysisResult);
                            return parseSentimentResult(analysisResult);
                        }
                    }
                }
            }

            throw new RuntimeException("OpenAI API 응답 파싱 실패");

        } catch (Exception e) {
            log.error("OpenAI 감성분석 실패: {}", e.getMessage());
            throw e;
        }
    }

    private String createSentimentPrompt(String title, String content) {
        return String.format("""
            다음 게시글의 감성과 성향을 분석해주세요.
            
            제목: %s
            내용: %s
            
            다음 형식으로 분석 결과를 JSON으로 응답해주세요:
            {
                "sentiment": "POSITIVE|NEGATIVE|NEUTRAL",
                "emotion": "기쁨|슬픔|화남|두려움|놀람|혐오|중립",
                "tone": "친근함|공식적|유머러스|진지함|비관적|낙관적|중립적",
                "summary": "감성분석 요약 (한 줄)",
                "confidence": 95
            }
            
            응답은 반드시 JSON 형식으로만 제공해주세요.
            """, title, content);
    }

    private SentimentResult parseSentimentResult(String analysisResult) {
        try {
            // 간단한 JSON 파싱 (실제로는 ObjectMapper 사용 권장)
            analysisResult = analysisResult.trim();
            if (analysisResult.startsWith("```json")) {
                analysisResult = analysisResult.substring(7);
            }
            if (analysisResult.endsWith("```")) {
                analysisResult = analysisResult.substring(0, analysisResult.length() - 3);
            }
            
            // 기본 파싱 로직 (실제로는 Jackson 사용 권장)
            String sentiment = extractValue(analysisResult, "sentiment");
            String emotion = extractValue(analysisResult, "emotion");
            String tone = extractValue(analysisResult, "tone");
            String summary = extractValue(analysisResult, "summary");
            String confidenceStr = extractValue(analysisResult, "confidence");
            
            int confidence = 85; // 기본값
            try {
                confidence = Integer.parseInt(confidenceStr.replaceAll("[^0-9]", ""));
            } catch (Exception e) {
                log.warn("신뢰도 파싱 실패, 기본값 사용");
            }

            return new SentimentResult(sentiment, emotion, tone, summary, confidence);

        } catch (Exception e) {
            log.error("감성분석 결과 파싱 실패: {}", e.getMessage());
            return new SentimentResult("NEUTRAL", "중립", "중립적", "감성분석 결과를 파싱할 수 없습니다.", 50);
        }
    }

    private String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            
            // 숫자값인 경우
            pattern = "\"" + key + "\"\\s*:\\s*([0-9]+)";
            p = java.util.regex.Pattern.compile(pattern);
            m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            
            return "알 수 없음";
        } catch (Exception e) {
            return "알 수 없음";
        }
    }

    private SentimentResult generateDemoSentiment(String title, String content) {
        // 데모용 감성분석 (키워드 기반)
        String text = (title + " " + content).toLowerCase();
        
        String sentiment = "NEUTRAL";
        String emotion = "중립";
        String tone = "중립적";
        String summary = "중립적인 성향의 게시글입니다.";
        int confidence = 75;

        // 긍정적 키워드 검사
        if (text.contains("좋") || text.contains("행복") || text.contains("기쁨") || 
            text.contains("사랑") || text.contains("감사") || text.contains("축하")) {
            sentiment = "POSITIVE";
            emotion = "기쁨";
            tone = "낙관적";
            summary = "긍정적이고 밝은 성향의 게시글입니다.";
            confidence = 80;
        }
        
        // 부정적 키워드 검사
        else if (text.contains("싫") || text.contains("화") || text.contains("슬픔") || 
                 text.contains("짜증") || text.contains("힘들") || text.contains("우울")) {
            sentiment = "NEGATIVE";
            emotion = "슬픔";
            tone = "비관적";
            summary = "부정적이거나 우울한 성향의 게시글입니다.";
            confidence = 78;
        }
        
        // 질문 형태
        else if (text.contains("?") || text.contains("질문") || text.contains("도움") || 
                 text.contains("문의")) {
            sentiment = "NEUTRAL";
            emotion = "호기심";
            tone = "친근함";
            summary = "질문이나 도움을 요청하는 게시글입니다.";
            confidence = 85;
        }

        return new SentimentResult(sentiment, emotion, tone, summary, confidence);
    }

    // 감성분석 결과 DTO
    public static class SentimentResult {
        private final String sentiment;
        private final String emotion;
        private final String tone;
        private final String summary;
        private final int confidence;

        public SentimentResult(String sentiment, String emotion, String tone, String summary, int confidence) {
            this.sentiment = sentiment;
            this.emotion = emotion;
            this.tone = tone;
            this.summary = summary;
            this.confidence = confidence;
        }

        public String getSentiment() { return sentiment; }
        public String getEmotion() { return emotion; }
        public String getTone() { return tone; }
        public String getSummary() { return summary; }
        public int getConfidence() { return confidence; }
        
        public String getSentimentColor() {
            return switch (sentiment) {
                case "POSITIVE" -> "#28a745";
                case "NEGATIVE" -> "#dc3545";
                default -> "#6c757d";
            };
        }
        
        public String getSentimentIcon() {
            return switch (sentiment) {
                case "POSITIVE" -> "😊";
                case "NEGATIVE" -> "😔";
                default -> "😐";
            };
        }
    }
}