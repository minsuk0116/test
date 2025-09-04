package com.jobatda.back.domain.board.service;

import com.jobatda.back.config.NaverApiProperties;
import com.jobatda.back.domain.news.dto.NaverNewsResponse;
import com.jobatda.back.domain.news.dto.NewsItem;
import com.jobatda.back.domain.news.dto.NewsPageResponse;
import com.jobatda.back.domain.news.dto.NewsSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverNewsService {
    
    private final RestTemplate restTemplate;
    private final Environment environment;
    private final NaverApiProperties naverApiProperties;
    
    @PostConstruct
    public void init() {
        log.info("=== 네이버 API 설정 확인 ===");
        
        // ConfigurationProperties로 주입된 값 확인
        log.info("ConfigurationProperties로 읽은 Client ID: {}", naverApiProperties.getClientId());
        log.info("ConfigurationProperties로 읽은 Client Secret: {}", naverApiProperties.getClientSecret());
        log.info("ConfigurationProperties로 읽은 API URL: {}", naverApiProperties.getNews().getUrl());
        
        // Environment로 직접 읽은 값 확인
        String envClientId = environment.getProperty("naver.api.client-id");
        String envClientSecret = environment.getProperty("naver.api.client-secret");
        String envApiUrl = environment.getProperty("naver.api.news.url");
        
        log.info("Environment로 읽은 Client ID: {}", envClientId);
        log.info("Environment로 읽은 Client Secret: {}", envClientSecret);
        log.info("Environment로 읽은 API URL: {}", envApiUrl);
        
        log.info("=== 최종 설정값 ===");
        String clientId = naverApiProperties.getClientId();
        String clientSecret = naverApiProperties.getClientSecret();
        String apiUrl = naverApiProperties.getNews().getUrl();
        
        log.info("Client ID: {}", clientId != null ? (clientId.length() > 5 ? clientId.substring(0, 5) + "..." : clientId) : "NULL");
        log.info("Client Secret: {}", clientSecret != null ? (clientSecret.length() > 3 ? clientSecret.substring(0, 3) + "..." : clientSecret) : "NULL");
        log.info("API URL: {}", apiUrl);
        
        if (clientId == null || clientId.trim().isEmpty()) {
            log.error("네이버 API Client ID가 설정되지 않았습니다!");
        }
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            log.error("네이버 API Client Secret이 설정되지 않았습니다!");
        }
    }
    
    // 기본 대기업들의 뉴스를 가져오는 메서드
    public List<NewsSearchResponse> getDefaultCompanyNews() {
        List<String> companies = Arrays.asList("삼성", "LG", "현대", "SK", "롯데");
        
        return companies.stream()
                .map(company -> {
                    try {
                        return searchNews(company);
                    } catch (Exception e) {
                        log.error("기본 뉴스 로딩 실패 - {}: {}", company, e.getMessage());
                        return NewsSearchResponse.builder()
                                .query(company)
                                .totalResults(0)
                                .news(List.of())
                                .build();
                    }
                })
                .toList();
    }
    
    // 특정 기업 뉴스 검색
    public NewsSearchResponse searchNews(String query) {
        try {
            log.info("뉴스 검색 시작: {}", query);
            
            // 더 정확한 검색을 위한 쿼리 개선
            String searchQuery = buildSearchQuery(query);
            
            // UriComponentsBuilder를 사용하여 안전한 URL 생성
            String url = UriComponentsBuilder.fromUriString(naverApiProperties.getNews().getUrl())
                    .queryParam("query", searchQuery)
                    .queryParam("display", 10)
                    .queryParam("start", 1)
                    .queryParam("sort", "sim") // 유사도순으로 변경
                    .toUriString();
            
            log.info("API URL: {}", url);
            log.info("Client ID exists: {}", naverApiProperties.getClientId() != null && !naverApiProperties.getClientId().isEmpty());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", naverApiProperties.getClientId());
            headers.set("X-Naver-Client-Secret", naverApiProperties.getClientSecret());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<NaverNewsResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, NaverNewsResponse.class);
            
            log.info("API 응답 상태: {}", response.getStatusCode());
            
            NaverNewsResponse naverResponse = response.getBody();
            
            if (naverResponse != null && naverResponse.getItems() != null) {
                log.info("검색 결과: {}건", naverResponse.getItems().size());
                
                // HTML 태그 제거 및 유효성 검증
                List<NewsItem> cleanedItems = naverResponse.getItems().stream()
                        .filter(item -> item != null && item.getTitle() != null && !item.getTitle().trim().isEmpty())
                        .map(this::cleanNewsItem)
                        .toList();
                
                return NewsSearchResponse.builder()
                        .query(query)
                        .totalResults(naverResponse.getTotal())
                        .news(cleanedItems)
                        .build();
            }
            
            log.warn("API 응답이 비어있음");
            return NewsSearchResponse.builder()
                    .query(query)
                    .totalResults(0)
                    .news(List.of())
                    .build();
                    
        } catch (Exception e) {
            log.error("뉴스 검색 중 오류 발생: {}", e.getMessage(), e);
            return NewsSearchResponse.builder()
                    .query(query)
                    .totalResults(0)
                    .news(List.of())
                    .build();
        }
    }

    // 페이징을 지원하는 뉴스 검색
    public NewsPageResponse searchNewsWithPaging(String query, int page, int size) {
        try {
            log.info("페이징 뉴스 검색 시작: {}, page: {}, size: {}", query, page, size);
            
            int start = (page * size) + 1;
            int display = Math.min(size, 100); // 네이버 API는 최대 100개까지 지원
            
            // 더 정확한 검색을 위한 쿼리 개선
            String searchQuery = buildSearchQuery(query);
            
            // UriComponentsBuilder를 사용하여 안전한 URL 생성
            String url = UriComponentsBuilder.fromUriString(naverApiProperties.getNews().getUrl())
                    .queryParam("query", searchQuery)
                    .queryParam("display", display)
                    .queryParam("start", start)
                    .queryParam("sort", "sim") // 유사도순으로 변경
                    .toUriString();
            
            log.info("페이징 API URL: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", naverApiProperties.getClientId());
            headers.set("X-Naver-Client-Secret", naverApiProperties.getClientSecret());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<NaverNewsResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, NaverNewsResponse.class);
            
            log.info("페이징 API 응답 상태: {}", response.getStatusCode());
            
            NaverNewsResponse naverResponse = response.getBody();
            
            if (naverResponse != null && naverResponse.getItems() != null) {
                List<NewsItem> cleanedItems = naverResponse.getItems().stream()
                        .filter(item -> item != null && item.getTitle() != null && !item.getTitle().trim().isEmpty())
                        .map(this::cleanNewsItem)
                        .toList();
                
                long totalElements = Math.min(naverResponse.getTotal(), 1000); // 네이버 API 제한
                int totalPages = (int) Math.ceil((double) totalElements / size);
                
                return NewsPageResponse.builder()
                        .news(cleanedItems)
                        .currentPage(page)
                        .totalPages(totalPages)
                        .totalElements(totalElements)
                        .size(size)
                        .first(page == 0)
                        .last(page >= totalPages - 1)
                        .hasNext(page < totalPages - 1)
                        .hasPrevious(page > 0)
                        .query(query)
                        .build();
            }
            
            return NewsPageResponse.builder()
                    .news(List.of())
                    .currentPage(page)
                    .totalPages(0)
                    .totalElements(0L)
                    .size(size)
                    .first(true)
                    .last(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .query(query)
                    .build();
                    
        } catch (Exception e) {
            log.error("뉴스 검색 중 오류 발생: {}", e.getMessage());
            return NewsPageResponse.builder()
                    .news(List.of())
                    .currentPage(page)
                    .totalPages(0)
                    .totalElements(0L)
                    .size(size)
                    .first(true)
                    .last(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .query(query)
                    .build();
        }
    }
    
    // HTML 태그 제거 및 텍스트 정리
    private NewsItem cleanNewsItem(NewsItem item) {
        String cleanDescription = cleanDescription(item.getDescription());
        
        return NewsItem.builder()
                .title(removeHtmlTags(item.getTitle()))
                .originallink(item.getOriginallink())
                .link(item.getLink())
                .description(cleanDescription)
                .pubDate(item.getPubDate())
                .build();
    }
    
    private String removeHtmlTags(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        
        return text
                .replaceAll("<[^>]*>", "") // HTML 태그 제거
                .replaceAll("&quot;", "\"") // HTML 엔티티 변환
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&[a-zA-Z]+;", "") // 기타 HTML 엔티티 제거
                // URL 패턴 제거 (더 강력한 패턴)
                .replaceAll("https?://[^\\s]+", "") 
                .replaceAll("www\\.[^\\s]+", "")
                .replaceAll("[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}[^\\s]*", "")
                // 경로/파일명 패턴 제거
                .replaceAll("/[a-zA-Z0-9%\\-_./]+", "")
                .replaceAll("\\.[a-zA-Z0-9%\\-_]+/[^\\s]*", "")
                // 특수 문자 패턴 정리
                .replaceAll("[%][0-9A-Fa-f]{2}", "") // URL 인코딩 제거
                .replaceAll("[-]{2,}", "-") // 연속 하이픈을 하나로
                .replaceAll("[.]{2,}", ".") // 연속 점을 하나로
                .replaceAll("\\s+", " ") // 연속된 공백을 하나로
                .replaceAll("^[\\s\\-\\./@#]+", "") // 시작 부분의 불필요한 문자 제거
                .replaceAll("[\\s\\-\\./@#]+$", "") // 끝 부분의 불필요한 문자 제거
                .trim();
    }
    
    // 뉴스 설명 전용 정리 메소드
    private String cleanDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "뉴스 내용을 확인해주세요.";
        }
        
        String cleaned = removeHtmlTags(description);
        
        // 추가 정리 작업
        cleaned = cleaned
                // 남은 파편적 텍스트 제거
                .replaceAll("com/[^\\s]*", "") // com으로 시작하는 경로
                .replaceAll("kr/[^\\s]*", "") // kr로 시작하는 경로
                .replaceAll("\\b[a-zA-Z]+\\.[a-zA-Z]{2,}[^\\s]*", "") // 도메인 패턴
                .replaceAll("\\b\\d{4}/\\d{2}/\\d{2}\\b", "") // 날짜 패턴
                .replaceAll("\\b\\d{4}년\\s*\\d{1,2}월\\s*\\d{1,2}일\\b", "") // 한국어 날짜
                .replaceAll("\\b[A-Z]{2,}\\b(?![가-힣])", "") // 연속 대문자 (한국어가 아닌 경우)
                .replaceAll("\\b[0-9a-fA-F]{8,}\\b", "") // 긴 해시값
                .replaceAll("[-_]{3,}", "") // 긴 구분자
                .replaceAll("\\s*-\\s*$", "") // 끝의 대시
                .replaceAll("^\\s*-\\s*", "") // 시작의 대시
                .replaceAll("\\s+", " ") // 공백 정리
                .trim();
        
        // 빈 문자열이 되었다면 기본 메시지 반환
        if (cleaned.trim().isEmpty()) {
            return "뉴스 내용을 확인해주세요.";
        }
        
        // 의미 없는 텍스트 패턴 확인
        if (cleaned.matches("^[\\s\\p{Punct}\\d]*$") || // 숫자와 특수문자만
            cleaned.matches("^[a-zA-Z\\s\\p{Punct}\\d]*$") && !cleaned.matches(".*[가-힣].*")) { // 영어만 있고 한국어 없음
            return "뉴스 내용을 확인해주세요.";
        }
        
        // 너무 짧은 경우 (15자 미만) 기본 메시지 반환
        if (cleaned.length() < 15) {
            return "뉴스 내용을 확인해주세요.";
        }
        
        // 최대 120자로 제한하고 마지막이 온전한 문장이 되도록 조정
        if (cleaned.length() > 120) {
            String shortened = cleaned.substring(0, 120);
            int lastSentenceEnd = Math.max(
                Math.max(shortened.lastIndexOf('.'), shortened.lastIndexOf('.')),
                Math.max(shortened.lastIndexOf('!'), shortened.lastIndexOf('?'))
            );
            
            if (lastSentenceEnd > 40) { // 최소한 40자는 보장
                cleaned = shortened.substring(0, lastSentenceEnd + 1);
            } else {
                // 마지막 완전한 단어에서 자르기
                int lastSpace = shortened.lastIndexOf(' ');
                if (lastSpace > 40) {
                    cleaned = shortened.substring(0, lastSpace) + "...";
                } else {
                    cleaned = shortened + "...";
                }
            }
        }
        
        return cleaned;
    }
    
    // 검색 쿼리 개선을 위한 메서드
    private String buildSearchQuery(String query) {
        String trimmedQuery = query.trim();
        
        // 네이버 API는 복잡한 OR/AND 쿼리에서 문제가 있을 수 있으므로 단순화
        // 주요 기업명에 대한 키워드 매핑
        if (trimmedQuery.contains("삼성")) {
            return "삼성전자";
        } else if (trimmedQuery.contains("LG")) {
            return "LG전자";
        } else if (trimmedQuery.contains("현대")) {
            return "현대자동차";
        } else if (trimmedQuery.contains("SK")) {
            return "SK텔레콤";
        } else if (trimmedQuery.contains("롯데")) {
            return "롯데";
        } else if (trimmedQuery.contains("네이버")) {
            return "네이버";
        } else if (trimmedQuery.contains("카카오")) {
            return "카카오";
        } else if (trimmedQuery.contains("쿠팡")) {
            return "쿠팡";
        } else if (trimmedQuery.contains("배달의민족")) {
            return "배달의민족";
        } else {
            // 기본 검색 쿼리는 단순하게
            return trimmedQuery;
        }
    }
}