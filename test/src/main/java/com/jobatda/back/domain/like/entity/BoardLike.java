package com.jobatda.back.domain.like.entity;

import com.jobatda.back.domain.board.entity.Board;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * BoardLike Entity 클래스
 * 데이터베이스의 'board_likes' 테이블과 매핑되는 JPA 엔티티
 * 
 * 역할:
 * - 게시글에 대한 좋아요 정보 저장
 * - 사용자별 게시글 좋아요 중복 방지 (Unique Constraint)
 * - 좋아요 취소 기능 지원 (데이터 삭제 방식)
 * 
 * 연관 관계:
 * - Board와 다대일(N:1) 관계: 한 게시글에 여러 좋아요
 * 
 * 중복 방지:
 * - (board_id, user_identifier) 조합으로 유니크 제약 조건 설정
 * - 동일 사용자가 같은 게시글에 중복 좋아요 불가능
 */
@Entity                                    // JPA 엔티티
@Table(name = "board_likes",               // 테이블명 지정
       uniqueConstraints = @UniqueConstraint(columnNames = {"board_id", "user_identifier"})) // 중복 방지
@Getter                                   // Getter 메서드 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본생성자
@AllArgsConstructor                      // 모든 필드 생성자
@Builder                                 // 빌더 패턴
@ToString(exclude = "board")             // toString에서 순환참조 방지
public class BoardLike {

    /**
     * 좋아요 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 좋아요가 속한 게시글 (외래키 관계)
     * N:1 관계 - 여러 좋아요가 하나의 게시글에 속함
     * LAZY 로딩: 필요할 때만 게시글 정보 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    /**
     * 사용자 식별자
     * 
     * 현재 구현:
     * - 단순 문자열로 사용자 구분 (IP, 세션ID, 임시ID 등)
     * 
     * 향후 개선점:
     * - User 엔티티 도입 시 @ManyToOne으로 변경 가능
     * - 로그인 시스템 연동 시 실제 사용자 ID 저장
     */
    @Column(name = "user_identifier", nullable = false, length = 100)
    private String userIdentifier;

    /**
     * 좋아요 생성 시간
     * @PrePersist에서 자동 설정됨
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 엔티티가 데이터베이스에 저장되기 전에 자동 호출
     * 좋아요 생성 시간을 현재 시간으로 설정
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}