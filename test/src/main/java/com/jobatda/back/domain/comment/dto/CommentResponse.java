package com.jobatda.back.domain.comment.dto;

import com.jobatda.back.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 응답 DTO
 * 
 * 역할:
 * - 클라이언트에게 댓글 정보를 응답할 때 사용
 * - 계층형 댓글 구조를 JSON으로 표현
 * - 순환 참조 문제 해결 (엔티티 직접 노출 방지)
 * 
 * 특징:
 * - 대댓글까지 포함한 전체 댓글 트리 구조 표현
 * - 재귀적인 children 필드로 무제한 깊이 지원
 */
@Getter        // 모든 필드의 Getter 메서드 자동 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 파라미터로 하는 생성자
@Builder       // 빌더 패턴으로 객체 생성 가능
public class CommentResponse {

    /** 댓글 고유 식별자 */
    private Long id;
    
    /** 댓글이 속한 게시글 ID */
    private Long boardId;
    
    /** 작성자 ID */
    private Long authorId;
    
    /** 작성자 닉네임 (게시글과 댓글에 표시됨) */
    private String authorNickname;
    
    /** 댓글 내용 */
    private String content;
    
    /** 댓글 작성 시간 */
    private LocalDateTime createdAt;
    
    /** 댓글 수정 시간 */
    private LocalDateTime updatedAt;
    
    /** 부모 댓글 ID (일반 댓글인 경우 null) */
    private Long parentId;
    
    /** 자식 댓글 목록 (대댓글들) - 재귀적 구조 */
    private List<CommentResponse> children;

    /**
     * Comment 엔티티를 CommentResponse DTO로 변환하는 정적 팩토리 메서드
     * 
     * @param comment 변환할 댓글 엔티티
     * @return 변환된 CommentResponse DTO
     * 
     * 특징:
     * - 재귀적으로 모든 자식 댓글(대댓글)도 함께 변환
     * - 순환 참조 문제 해결 (parent는 ID만 포함)
     * - Stream API를 사용한 함수형 프로그래밍 스타일
     */
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .boardId(comment.getBoard().getId())
                .authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                .authorNickname(comment.getAuthorNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .children(comment.getChildren().stream()  // 자식 댓글들을 재귀적으로 변환
                        .map(CommentResponse::from)       // 메서드 참조를 통한 재귀 호출
                        .collect(Collectors.toList()))
                .build();
    }
}