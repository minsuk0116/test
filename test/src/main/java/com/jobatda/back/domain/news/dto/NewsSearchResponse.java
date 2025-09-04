package com.jobatda.back.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSearchResponse {
    private String query;          // 검색어
    private int totalResults;      // 총 결과 수
    private List<NewsItem> news;   // 뉴스 목록
}