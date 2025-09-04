package com.jobatda.back.domain.board.dto;

// 연관된 엔티티와 타입들
import com.jobatda.back.domain.board.entity.Board;     // 게시글 엔티티
import com.jobatda.back.domain.board.entity.BoardType; // 게시판 타입 열거형

// Lombok
import lombok.Builder; // 빌더 패턴
import lombok.Getter;  // Getter 메서드 자동 생성

/**
 * BoardResponse DTO 클래스
 * 게시글 정보를 클라이언트에게 응답할 때 사용하는 데이터 전송 객체
 * 
 * DTO(Data Transfer Object) 사용 이유:
 * 1. 엔티티 직접 노출 방지 - 내부 구조 은닉
 * 2. 순환 참조 방지 - JSON 직렬화 문제 해결
 * 3. 필요한 데이터만 선별적 전송 - 네트워크 효율성
 * 4. API 스펙 안정성 - 엔티티 변경이 API에 미치는 영향 최소화
 * 
 * Response용 DTO 특징:
 * - 읽기 전용 (Getter만 제공)
 * - 정적 팩토리 메서드로 엔티티 → DTO 변환
 * - 계산된 값들 포함 가능 (likeCount, commentCount 등)
 */
@Getter  // 모든 필드의 Getter 메서드 자동 생성
@Builder // 빌더 패턴으로 객체 생성 지원
public class BoardResponse {
    // 게시글 기본 정보
    private Long id;           // 게시글 ID
    private String title;      // 제목
    private String content;    // 내용
    private BoardType boardType; // 게시판 타입
    private String imageUrl;   // AI 생성 이미지 URL
    
    // 작성자 정보
    private Long authorId;     // 작성자 ID
    private String authorNickname; // 작성자 닉네임 (게시글과 댓글에 표시됨)
    
    // 추가 정보 (다른 엔티티들과의 관계에서 계산된 값들)
    private Long likeCount;    // 좋아요 수
    private Long commentCount; // 댓글 수

    /**
     * 엔티티를 DTO로 변환하는 정적 팩토리 메서드
     * 
     * @param board 변환할 게시글 엔티티
     * @return 변환된 BoardResponse DTO
     * 
     * 정적 팩토리 메서드 장점:
     * - 메서드명으로 의도 명확화 (from, of, valueOf 등)
     * - 생성자보다 유연함
     * - 캐싱이나 싱글톤 패턴 적용 가능
     */
    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .boardType(board.getBoardType())
                .imageUrl(board.getImageUrl())
                .authorId(board.getAuthor() != null ? board.getAuthor().getId() : null)
                .authorNickname(board.getAuthorNickname())
                .likeCount(0L) // 기본값, 서비스에서 설정
                .commentCount(0L) // 기본값, 서비스에서 설정
                .build();
    }

    /**
     * 좋아요 수와 댓글 수를 포함해서 BoardResponse를 생성하는 정적 팩토리 메서드
     * 게시글 목록이나 상세 조회 시 통계 정보를 함께 제공할 때 사용
     * 
     * @param board 변환할 게시글 엔티티
     * @param likeCount 좋아요 수
     * @param commentCount 댓글 수
     * @return 통계 정보가 포함된 BoardResponse DTO
     */
    public static BoardResponse of(Board board, Long likeCount, Long commentCount) {
        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .boardType(board.getBoardType())
                .imageUrl(board.getImageUrl())
                .authorId(board.getAuthor() != null ? board.getAuthor().getId() : null)
                .authorNickname(board.getAuthorNickname())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .build();
    }
}
