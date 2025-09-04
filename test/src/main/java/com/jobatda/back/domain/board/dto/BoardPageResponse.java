package com.jobatda.back.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게시글 목록 페이징 응답 DTO
 * 
 * 역할:
 * - 페이징된 게시글 목록을 클라이언트에게 응답할 때 사용
 * - Spring Data JPA의 Page 객체를 프론트엔드가 이해하기 쉬운 형태로 변환
 * - 페이징 관련 메타데이터를 포함하여 UI에서 페이지네이션 구현 지원
 * 
 * 사용 시나리오:
 * - 게시글 목록 조회 API (/api/boards?page=0&size=10)
 * - 특정 게시판 타입별 목록 조회
 * - 검색 결과 페이징 처리
 */
@Getter        // 모든 필드의 Getter 메서드 자동 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 파라미터로 하는 생성자
@Builder       // 빌더 패턴으로 객체 생성 가능
public class BoardPageResponse {
    
    /** 현재 페이지의 게시글 목록 */
    private List<BoardResponse> boards;
    
    /** 현재 페이지 번호 (0부터 시작) */
    private int currentPage;
    
    /** 전체 페이지 수 */
    private int totalPages;
    
    /** 전체 게시글 수 */
    private long totalElements;
    
    /** 한 페이지당 게시글 수 */
    private int size;
    
    /** 첫 번째 페이지 여부 */
    private boolean first;
    
    /** 마지막 페이지 여부 */
    private boolean last;
    
    /** 다음 페이지 존재 여부 */
    private boolean hasNext;
    
    /** 이전 페이지 존재 여부 */
    private boolean hasPrevious;
}