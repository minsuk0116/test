package com.jobatda.back.domain.board.dto;

import com.jobatda.back.domain.board.entity.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 수정 요청 DTO (Data Transfer Object)
 * 
 * 역할:
 * - 클라이언트에서 게시글 수정 시 전달되는 데이터를 담는 객체
 * - HTTP PUT/PATCH 요청 본문을 자바 객체로 변환
 * - Bean Validation을 통해 수정 데이터의 유효성 검증
 * 
 * 특징:
 * - 생성 요청과 달리 ID는 포함하지 않음 (URL path parameter로 전달)
 * - 현재 버전에서는 이미지 URL 수정 기능 제외 (별도 API로 처리)
 */
@Getter  // 모든 필드의 Getter 메서드 자동 생성
@Setter  // 모든 필드의 Setter 메서드 자동 생성 (JSON 역직렬화를 위해 필요)
@NoArgsConstructor // 기본 생성자 (JSON 역직렬화를 위해 필요)
@AllArgsConstructor // 모든 필드를 파라미터로 하는 생성자
@Builder // 빌더 패턴으로 객체 생성 가능
public class BoardUpdateRequest {
    
    /**
     * 수정할 게시글 제목
     * 빈 값이나 공백만으로는 수정 불가능
     */
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    
    /**
     * 수정할 게시글 내용
     * 빈 값이나 공백만으로는 수정 불가능
     */
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    /**
     * 수정할 게시판 타입 (NOTICE, FREE, QNA)
     * null 값으로는 수정 불가능
     */
    @NotNull(message = "게시판 유형은 필수입니다")
    private BoardType boardType;
}