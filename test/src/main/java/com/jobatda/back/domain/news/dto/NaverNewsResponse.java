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
public class NaverNewsResponse {
    private String lastBuildDate;  // 검색 결과를 생성한 시간
    private int total;             // 총 검색 결과 개수
    private int start;             // 검색 시작 위치
    private int display;           // 한 번에 표시할 검색 결과 개수
    private List<NewsItem> items;  // 뉴스 목록
}