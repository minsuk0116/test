package com.jobatda.back.domain.sentiment.controller;

import com.jobatda.back.domain.board.entity.Board;
import com.jobatda.back.domain.board.repository.BoardRepository;
import com.jobatda.back.domain.sentiment.service.SentimentAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sentiment")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SentimentController {

    private final SentimentAnalysisService sentimentAnalysisService;
    private final BoardRepository boardRepository;

    @GetMapping("/analyze/{boardId}")
    public ResponseEntity<Map<String, Object>> analyzeBoardSentiment(@PathVariable Long boardId) {
        try {
            log.info("게시글 감성분석 요청 - boardId: {}", boardId);

            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

            SentimentAnalysisService.SentimentResult result = 
                    sentimentAnalysisService.analyzeSentiment(board.getTitle(), board.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sentiment", result.getSentiment());
            response.put("emotion", result.getEmotion());
            response.put("tone", result.getTone());
            response.put("summary", result.getSummary());
            response.put("confidence", result.getConfidence());
            response.put("color", result.getSentimentColor());
            response.put("icon", result.getSentimentIcon());

            log.info("감성분석 완료 - 결과: {}", result.getSentiment());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("감성분석 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "감성분석 중 오류가 발생했습니다.");
            errorResponse.put("sentiment", "NEUTRAL");
            errorResponse.put("emotion", "중립");
            errorResponse.put("tone", "중립적");
            errorResponse.put("summary", "감성분석을 수행할 수 없습니다.");
            errorResponse.put("confidence", 0);
            errorResponse.put("color", "#6c757d");
            errorResponse.put("icon", "😐");

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeText(@RequestBody Map<String, String> request) {
        try {
            String title = request.getOrDefault("title", "");
            String content = request.getOrDefault("content", "");

            if (title.isEmpty() && content.isEmpty()) {
                throw new RuntimeException("분석할 텍스트가 없습니다.");
            }

            SentimentAnalysisService.SentimentResult result = 
                    sentimentAnalysisService.analyzeSentiment(title, content);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sentiment", result.getSentiment());
            response.put("emotion", result.getEmotion());
            response.put("tone", result.getTone());
            response.put("summary", result.getSummary());
            response.put("confidence", result.getConfidence());
            response.put("color", result.getSentimentColor());
            response.put("icon", result.getSentimentIcon());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("텍스트 감성분석 실패: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "감성분석 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}