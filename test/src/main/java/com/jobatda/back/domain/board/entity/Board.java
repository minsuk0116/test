package com.jobatda.back.domain.board.entity;

// JPA(Jakarta Persistence API) 관련 어노테이션들
import jakarta.persistence.Column;           // 데이터베이스 컬럼 매핑
import jakarta.persistence.Entity;           // JPA 엔티티임을 명시
import jakarta.persistence.EnumType;         // Enum 타입 저장 방식 설정
import jakarta.persistence.Enumerated;       // Enum 필드 매핑
import jakarta.persistence.FetchType;        // 연관관계 페치 타입 설정
import jakarta.persistence.GeneratedValue;   // 기본키 자동 생성
import jakarta.persistence.GenerationType;   // 기본키 생성 전략
import jakarta.persistence.Id;              // 기본키 필드 표시
import jakarta.persistence.JoinColumn;       // 외래키 컬럼 매핑
import jakarta.persistence.ManyToOne;        // 다대일 연관관계 매핑
import jakarta.persistence.Table;           // 데이터베이스 테이블명 매핑

// User 엔티티 import
import com.jobatda.back.domain.user.entity.User;

// Lombok 어노테이션들 - 코드 간소화
import lombok.AccessLevel;      // 접근 제한자 설정
import lombok.AllArgsConstructor; // 모든 필드 생성자
import lombok.Builder;          // 빌더 패턴
import lombok.Getter;           // Getter 메서드 자동 생성
import lombok.NoArgsConstructor; // 기본 생성자
import lombok.ToString;         // toString 메서드 자동 생성

/**
 * Board Entity 클래스
 * 데이터베이스의 'boards' 테이블과 매핑되는 JPA 엔티티
 * 
 * JPA Entity 역할:
 * - 데이터베이스 테이블의 구조와 1:1 매핑
 * - 데이터의 영속성(persistence) 관리
 * - 객체-관계 매핑(ORM)의 핵심 요소
 * 
 * 게시글의 핵심 정보를 담는 도메인 객체
 */
@Entity                                        // JPA 엔티티임을 선언
@Table(name = "boards")                       // 데이터베이스 테이블명 지정
@Getter                                       // 모든 필드의 Getter 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본생성자 (외부 직접 생성 방지)
@AllArgsConstructor                          // 모든 필드를 파라미터로 하는 생성자
@Builder                                     // 빌더 패턴으로 객체 생성 가능
@ToString                                    // toString 메서드 자동 생성
public class Board {

    /**
     * 게시글의 고유 식별자 (Primary Key)
     * 데이터베이스에서 자동으로 증가되는 값
     */
    @Id                                      // 기본키 필드 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT 사용
    private Long id;

    /**
     * 게시판 타입 (자유게시판, 공지사항, Q&A)
     * Enum을 문자열로 데이터베이스에 저장
     */
    @Enumerated(EnumType.STRING)             // Enum을 문자열로 저장 (가독성 및 확장성 좋음)
    @Column(nullable = false)                // NOT NULL 제약 조건
    private BoardType boardType;

    /**
     * 게시글 제목
     * 최대 100자까지 저장 가능
     */
    @Column(nullable = false, length = 100)  // NOT NULL, 최대길이 100자
    private String title;

    /**
     * 게시글 내용
     * TEXT 타입으로 긴 내용도 저장 가능
     */
    @Column(nullable = false, columnDefinition = "TEXT") // TEXT 타입으로 지정
    private String content;

    /**
     * 게시글에 첨부된 이미지 URL
     * 최대 500자까지 저장 가능 (NULL 허용)
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * 게시글 작성자 정보
     * User 엔티티와의 다대일(Many-to-One) 연관관계
     * 
     * 연관관계 설정:
     * - 여러 게시글이 하나의 사용자에 의해 작성될 수 있음 (N:1)
     * - LAZY 로딩: 게시글 조회 시 작성자 정보는 필요할 때만 로딩
     * - 외래키: author_id 컬럼으로 User 테이블의 id와 연결
     */
    @ManyToOne(fetch = FetchType.LAZY)    // 지연 로딩으로 성능 최적화
    @JoinColumn(name = "author_id")       // 외래키 컬럼명 지정
    private User author;

    // === 도메인 로직 메서드들 ===
    
    /**
     * 게시글 정보를 업데이트하는 메서드 (이미지 제외)
     * 엔티티의 불변성을 보장하면서 필드를 안전하게 변경
     * 작성자는 변경되지 않음 (게시글 수정 시에는 작성자가 바뀌지 않기 때문)
     * 
     * @param boardType 변경할 게시판 타입
     * @param title 변경할 제목
     * @param content 변경할 내용
     */
    public void update(BoardType boardType, String title, String content) {
        this.boardType = boardType;
        this.title = title;
        this.content = content;
    }
    
    /**
     * 게시글 정보를 업데이트하는 메서드 (이미지 포함)
     * 이미지 URL까지 함께 업데이트할 때 사용
     * 작성자는 변경되지 않음
     * 
     * @param boardType 변경할 게시판 타입
     * @param title 변경할 제목
     * @param content 변경할 내용
     * @param imageUrl 변경할 이미지 URL
     */
    public void updateWithImage(BoardType boardType, String title, String content, String imageUrl) {
        this.boardType = boardType;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    /**
     * 게시글 작성자가 특정 사용자와 같은지 확인하는 메서드
     * 게시글 수정/삭제 권한 체크에 사용
     * 
     * @param user 확인할 사용자
     * @return 작성자와 같으면 true, 다르면 false
     */
    public boolean isAuthor(User user) {
        return this.author != null && this.author.getId().equals(user.getId());
    }
    
    /**
     * 게시글 작성자의 닉네임을 반환하는 메서드
     * 작성자 정보가 없는 경우 "알 수 없는 사용자" 반환
     * 
     * @return 작성자 닉네임 또는 기본값
     */
    public String getAuthorNickname() {
        return this.author != null ? this.author.getNickname() : "알 수 없는 사용자";
    }
}
