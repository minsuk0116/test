package com.jobatda.back.domain.news.controller;

import com.jobatda.back.domain.board.service.NaverNewsService;
import com.jobatda.back.domain.news.dto.NewsPageResponse;
import com.jobatda.back.domain.news.dto.NewsSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NewsController 클래스
 * 뉴스 검색 관련 REST API를 제공하는 컨트롤러
 * 
 * 주요 기능:
 * - 네이버 뉴스 API를 활용한 뉴스 검색
 * - 기본 대기업 뉴스 조회
 * - 특정 키워드 뉴스 검색 (일반 검색 및 페이징 검색)
 * - 예외 처리를 통한 안정적인 API 응답
 * 
 * API 엔드포인트:
 * - GET /api/news/default : 기본 대기업 뉴스 목록
 * - GET /api/news/search : 키워드 뉴스 검색
 * - GET /api/news/search/page : 페이징된 키워드 뉴스 검색
 */
@Slf4j                           // 로깅 기능 자동 추가
@RestController                  // JSON 응답을 하는 REST API 컨트롤러
@RequestMapping("/api/news")     // 모든 메서드의 URL이 /api/news로 시작
@RequiredArgsConstructor         // final 필드에 대한 의존성 주입 생성자 자동 생성
public class NewsController {

    /**
     * 네이버 뉴스 API를 호출하는 서비스
     * 실제 뉴스 검색 로직을 처리
     */
    private final NaverNewsService naverNewsService;

    /**
     * 기본 대기업 뉴스를 조회하는 API
     * 
     * HTTP GET /api/news/default
     * 
     * @return 기본 설정된 대기업들의 최신 뉴스 목록
     * 
     * 특징:
     * - 미리 정의된 대기업 키워드들로 뉴스 검색
     * - 예외 발생 시 빈 리스트 반환으로 안정적인 응답 보장
     */
    @GetMapping("/default")
    public List<NewsSearchResponse> getDefaultCompanyNews() {
        log.info("기본 대기업 뉴스 요청");
        try {
            return naverNewsService.getDefaultCompanyNews();
        } catch (Exception e) {
            log.error("기본 뉴스 조회 실패: {}", e.getMessage(), e);
            return List.of(); // 빈 리스트 반환
        }
    }

    /**
     * 특정 키워드로 뉴스를 검색하는 API (기존 API 호환성 유지)
     * 
     * HTTP GET /api/news/search?query=키워드
     * 
     * @param query 검색할 키워드 (기업명, 업종 등)
     * @return 검색된 뉴스 목록과 검색 결과 정보
     * 
     * 입력값 검증:
     * - 빈 문자열이나 null인 경우 빈 결과 반환
     * - 앞뒤 공백 자동 제거
     */
    @GetMapping("/search")
    public NewsSearchResponse searchCompanyNews(@RequestParam String query) {
        log.info("기업 뉴스 검색 요청: {}", query);
        try {
            if (query == null || query.trim().isEmpty()) {
                return NewsSearchResponse.builder()
                        .query(query)
                        .totalResults(0)
                        .news(List.of())
                        .build();
            }
            return naverNewsService.searchNews(query.trim());
        } catch (Exception e) {
            log.error("뉴스 검색 실패 - {}: {}", query, e.getMessage(), e);
            return NewsSearchResponse.builder()
                    .query(query)
                    .totalResults(0)
                    .news(List.of())
                    .build();
        }
    }

    /**
     * 페이징 처리된 뉴스 검색 API
     * 
     * HTTP GET /api/news/search/page?query=키워드&page=0&size=10
     * 
     * @param query 검색할 키워드
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 한 페이지당 뉴스 수 (기본값: 10, 최대: 100)
     * @return 페이징 정보가 포함된 뉴스 검색 결과
     * 
     * 파라미터 검증:
     * - page < 0인 경우 0으로 조정
     * - size가 0 이하이거나 100 초과인 경우 10으로 조정
     * - 빈 query인 경우 빈 결과 페이지 반환
     */
    @GetMapping("/search/page")
    public NewsPageResponse searchCompanyNewsWithPaging(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("기업 뉴스 페이징 검색 요청: {}, page: {}, size: {}", query, page, size);
        try {
            if (query == null || query.trim().isEmpty()) {
                return NewsPageResponse.builder()
                        .query(query)
                        .news(List.of())
                        .currentPage(page)
                        .totalPages(0)
                        .totalElements(0L)
                        .size(size)
                        .first(true)
                        .last(true)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build();
            }
            // 페이지와 사이즈 유효성 검증
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 10;
            
            return naverNewsService.searchNewsWithPaging(query.trim(), page, size);
        } catch (Exception e) {
            log.error("페이징 뉴스 검색 실패 - {}: {}", query, e.getMessage(), e);
            return NewsPageResponse.builder()
                    .query(query)
                    .news(List.of())
                    .currentPage(page)
                    .totalPages(0)
                    .totalElements(0L)
                    .size(size)
                    .first(true)
                    .last(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
        }
    }
}