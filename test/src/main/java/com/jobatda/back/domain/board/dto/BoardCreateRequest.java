package com.jobatda.back.domain.board.dto;

import com.jobatda.back.domain.board.entity.Board;
import com.jobatda.back.domain.board.entity.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 생성 요청 DTO (Data Transfer Object)
 * 
 * 역할:
 * - 클라이언트에서 게시글 생성 시 전달되는 데이터를 담는 객체
 * - HTTP 요청 본문(Request Body)을 자바 객체로 변환
 * - Bean Validation을 통해 데이터 유효성 검증
 * - Entity 변환 메서드 제공으로 비즈니스 로직 분리
 */
@Getter  // 모든 필드의 Getter 메서드 자동 생성
@Setter  // 모든 필드의 Setter 메서드 자동 생성 (JSON 역직렬화를 위해 필요)
@NoArgsConstructor // 기본 생성자 (JSON 역직렬화를 위해 필요)
@AllArgsConstructor // 모든 필드를 파라미터로 하는 생성자
@Builder // 빌더 패턴으로 객체 생성 가능
public class BoardCreateRequest {
    
    /**
     * 게시글 제목
     * 반드시 입력되어야 하며, 빈 문자열이나 공백만으로는 불가능
     */
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    
    /**
     * 게시글 내용
     * 반드시 입력되어야 하며, 빈 문자열이나 공백만으로는 불가능
     */
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    /**
     * 게시판 타입 (NOTICE, FREE, QNA)
     * null 값은 허용되지 않음
     */
    @NotNull(message = "게시판 유형은 필수입니다")
    private BoardType boardType;
    
    /**
     * 이미지 URL (선택사항)
     * 게시글에 첨부할 이미지가 있을 경우에만 입력
     */
    private String imageUrl;
    
    /**
     * 작성자 ID
     * 게시글을 작성하는 사용자의 ID
     */
    @NotNull(message = "작성자 ID는 필수입니다")
    private Long authorId;

    /**
     * DTO를 Entity로 변환하는 메서드
     * 
     * @return 생성된 Board 엔티티 객체
     */
    public Board toEntity() {
        return Board.builder()
                .title(title)
                .content(content)
                .boardType(boardType)
                .imageUrl(imageUrl)
                .build();
    }
}