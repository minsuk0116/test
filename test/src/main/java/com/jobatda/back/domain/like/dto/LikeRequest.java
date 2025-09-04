package com.jobatda.back.domain.like.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좋아요 요청 DTO
 * 
 * 역할:
 * - 게시글 좋아요 등록/취소 시 사용되는 요청 데이터
 * - 사용자 식별을 위한 정보 전달
 * 
 * 사용 시나리오:
 * - POST /boards/{boardId}/likes : 좋아요 등록
 * - DELETE /boards/{boardId}/likes : 좋아요 취소
 * 
 * 향후 개선점:
 * - 인증 시스템 도입 시 JWT 토큰 등으로 사용자 식별 가능
 * - 현재는 클라이언트에서 전달하는 식별자에 의존
 */
@Getter        // 모든 필드의 Getter 메서드 자동 생성
@NoArgsConstructor // 기본 생성자 (JSON 역직렬화를 위해 필요)
public class LikeRequest {

    /**
     * 사용자 식별자
     * 
     * 현재 구현:
     * - 클라이언트에서 전달하는 임시 식별자 (IP, 세션ID, UUID 등)
     * 
     * 보안 고려사항:
     * - 실제 서비스에서는 인증된 사용자 정보 사용 권장
     * - 현재 방식은 임시 사용자 구분 목적으로만 적합
     */
    @NotBlank(message = "사용자 식별자는 필수입니다.")
    private String userIdentifier;
}