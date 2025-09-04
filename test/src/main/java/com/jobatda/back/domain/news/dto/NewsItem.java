package com.jobatda.back.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsItem {
    private String title;        // 뉴스 제목
    private String originallink; // 뉴스 원본 링크
    private String link;         // 네이버 뉴스 링크
    private String description;  // 뉴스 내용 요약
    private String pubDate;      // 뉴스 발행일
}