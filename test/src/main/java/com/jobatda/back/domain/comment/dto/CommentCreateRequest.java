package com.jobatda.back.domain.comment.dto;

import com.jobatda.back.domain.board.entity.Board;
import com.jobatda.back.domain.comment.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 생성 요청 DTO
 * 
 * 역할:
 * - 클라이언트에서 댓글 생성 시 전달되는 데이터를 담는 객체
 * - 일반 댓글과 대댓글 생성 모두 지원
 * - Bean Validation을 통한 데이터 유효성 검증
 * 
 * 계층형 댓글 구조:
 * - parentId가 null: 일반 댓글
 * - parentId가 있음: 대댓글 (부모 댓글에 대한 응답)
 */
@Getter        // 모든 필드의 Getter 메서드 자동 생성
@NoArgsConstructor // 기본 생성자 (JSON 역직렬화를 위해 필요)
public class CommentCreateRequest {

    // 작성자 정보는 서버에서 현재 로그인한 사용자로 설정하므로 제거

    /**
     * 댓글 내용
     * 빈 문자열이나 공백만으로는 댓글 작성 불가능
     */
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    /**
     * 부모 댓글 ID (대댓글인 경우에만 설정)
     * - null: 일반 댓글 (게시글에 직접 작성)
     * - Long 값: 해당 ID를 가진 댓글에 대한 대댓글
     */
    private Long parentId;

    /**
     * DTO를 Comment 엔티티로 변환하는 메서드
     * 작성자 정보는 별도로 설정해야 함
     * 
     * @param board 댓글이 속할 게시글
     * @param parent 부모 댓글 (일반 댓글인 경우 null)
     * @return 생성된 Comment 엔티티 (작성자 제외)
     */
    public Comment toEntity(Board board, Comment parent) {
        return Comment.builder()
                .board(board)
                .content(content)
                .parent(parent)  // null이면 일반 댓글, 값이 있으면 대댓글
                .build();
    }
}