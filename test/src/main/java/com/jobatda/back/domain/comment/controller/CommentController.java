package com.jobatda.back.domain.comment.controller;

// 댓글 관련 DTO들
import com.jobatda.back.domain.comment.dto.CommentCreateRequest;  // 댓글 생성 요청 DTO
import com.jobatda.back.domain.comment.dto.CommentResponse;       // 댓글 응답 DTO  
import com.jobatda.back.domain.comment.dto.CommentUpdateRequest;  // 댓글 수정 요청 DTO
import com.jobatda.back.domain.comment.service.CommentService;    // 댓글 비즈니스 로직 서비스

// Spring 관련
import jakarta.validation.Valid;        // 유효성 검증
import lombok.RequiredArgsConstructor;  // 생성자 주입
import lombok.extern.slf4j.Slf4j;       // 로깅
import org.springframework.web.bind.annotation.*; // 웹 MVC 어노테이션들

// Java 기본 라이브러리
import java.util.List;
import java.util.stream.Collectors;

/**
 * CommentController 클래스
 * 댓글 관련 REST API를 제공하는 컨트롤러
 * 
 * RESTful URL 설계 패턴:
 * - /boards/{boardId}/comments : 특정 게시글의 댓글들
 * - 계층적 리소스 구조로 명확한 관계 표현
 * 
 * 주요 기능:
 * - 댓글 CRUD (생성, 조회, 수정, 삭제)
 * - 특정 게시글의 댓글 목록 조회
 * - 댓글 수 카운팅
 * - 계층형 댓글(대댓글) 지원
 */
@RestController                                    // JSON 응답 REST API 컨트롤러
@RequestMapping("/boards/{boardId}/comments")      // 기본 URL 매핑: 게시글별 댓글 엔드포인트
@RequiredArgsConstructor                          // final 필드 생성자 주입
@Slf4j                                           // 로깅 기능 추가
public class CommentController {

    /**
     * 댓글 비즈니스 로직을 처리하는 서비스
     * final로 불변성 보장, 생성자 주입을 통한 의존성 주입
     */
    private final CommentService commentService;

    /**
     * 새로운 댓글을 생성하는 API 엔드포인트
     * 
     * HTTP POST /boards/{boardId}/comments
     * 
     * @param boardId 댓글을 작성할 게시글의 ID (URL 경로에서 추출)
     * @param request 댓글 생성에 필요한 데이터 (내용, 부모댓글ID 등)
     * @param authorId 댓글 작성자의 사용자 ID (TODO: 추후 JWT 인증에서 자동으로 추출하도록 변경)
     * @return 생성된 댓글 정보
     * 
     * @PathVariable: URL 경로의 {boardId} 값을 파라미터로 받음
     * @Valid: 댓글 내용 필수, 길이 제한 등 유효성 검증
     */
    @PostMapping
    public CommentResponse createComment(@PathVariable Long boardId,
                                       @Valid @RequestBody CommentCreateRequest request,
                                       @RequestParam Long authorId) {
        log.info("댓글 생성 요청 - boardId: {}, authorId: {}", boardId, authorId);
        return CommentResponse.from(commentService.createComment(boardId, request, authorId));
    }

    /**
     * 특정 게시글의 모든 댓글을 조회하는 API
     * 
     * HTTP GET /boards/{boardId}/comments
     * 
     * @param boardId 댓글을 조회할 게시글의 ID
     * @return 해당 게시글의 댓글 목록 (계층 구조 포함)
     * 
     * Stream API 활용: 엔티티 리스트를 DTO 리스트로 변환
     */
    @GetMapping
    public List<CommentResponse> getComments(@PathVariable Long boardId) {
        return commentService.getCommentsByBoardId(boardId).stream()
                .map(CommentResponse::from)           // 메서드 참조로 DTO 변환
                .collect(Collectors.toList());        // List로 수집
    }

    /**
     * 기존 댓글을 수정하는 API
     * 
     * HTTP PUT /boards/{boardId}/comments/{commentId}
     * 
     * @param boardId 게시글 ID (컨텍스트 제공용)
     * @param commentId 수정할 댓글의 ID
     * @param request 수정할 내용이 담긴 요청 데이터
     * @return 수정된 댓글 정보
     */
    @PutMapping("/{commentId}")
    public CommentResponse updateComment(@PathVariable Long boardId,
                                       @PathVariable Long commentId,
                                       @Valid @RequestBody CommentUpdateRequest request) {
        log.info("댓글 수정 요청 - commentId: {}", commentId);
        return CommentResponse.from(commentService.updateComment(commentId, request));
    }

    /**
     * 댓글을 삭제하는 API
     * 
     * HTTP DELETE /boards/{boardId}/comments/{commentId}
     * 
     * @param boardId 게시글 ID
     * @param commentId 삭제할 댓글의 ID
     * 
     * void 반환: 성공 시 204 No Content, 실패 시 예외 발생
     * 계층형 댓글의 경우 자식 댓글들의 처리 방식도 고려해야 함
     */
    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long boardId,
                            @PathVariable Long commentId) {
        log.info("댓글 삭제 요청 - commentId: {}", commentId);
        commentService.deleteComment(commentId);
    }

    /**
     * 특정 게시글의 댓글 수를 조회하는 API
     * 
     * HTTP GET /boards/{boardId}/comments/count
     * 
     * @param boardId 댓글 수를 조회할 게시글의 ID
     * @return 해당 게시글의 총 댓글 수
     * 
     * 프론트엔드에서 댓글 수 표시나 페이징 처리에 활용
     */
    @GetMapping("/count")
    public Long getCommentCount(@PathVariable Long boardId) {
        return commentService.getCommentCount(boardId);
    }
}