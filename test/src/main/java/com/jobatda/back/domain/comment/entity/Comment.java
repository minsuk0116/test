package com.jobatda.back.domain.comment.entity;

// 연관된 엔티티
import com.jobatda.back.domain.board.entity.Board;
import com.jobatda.back.domain.user.entity.User;

// JPA 관련
import jakarta.persistence.*;

// Lombok - 코드 간소화
import lombok.*;

// 날짜/시간 처리
import java.time.LocalDateTime;
// Java 컬렉션
import java.util.ArrayList;
import java.util.List;

/**
 * Comment Entity 클래스
 * 데이터베이스의 'comments' 테이블과 매핑되는 JPA 엔티티
 * 
 * 계층형 댓글 구조 지원:
 * - 부모 댓글과 자식 댓글(대댓글) 관계
 * - Self-Join을 통한 트리 구조 구현
 * 
 * 연관 관계:
 * - Board와 다대일(N:1) 관계: 한 게시글에 여러 댓글
 * - Comment와 일대다(1:N) 관계: 한 댓글에 여러 대댓글
 */
@Entity                                        // JPA 엔티티
@Table(name = "comments")                     // 테이블명 지정
@Getter                                       // Getter 메서드 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본생성자
@AllArgsConstructor                          // 모든 필드 생성자
@Builder                                     // 빌더 패턴
@ToString(exclude = {"board", "parent", "children"}) // toString에서 순환참조 방지
public class Comment {

    /**
     * 댓글 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 댓글이 속한 게시글 (외래키 관계)
     * N:1 관계 - 여러 댓글이 하나의 게시글에 속함
     * LAZY 로딩: 필요할 때만 게시글 정보 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    /**
     * 댓글 작성자 정보
     * User 엔티티와의 다대일(Many-to-One) 연관관계
     * 
     * 연관관계 설정:
     * - 여러 댓글이 하나의 사용자에 의해 작성될 수 있음 (N:1)
     * - LAZY 로딩: 댓글 조회 시 작성자 정보는 필요할 때만 로딩
     * - 외래키: author_id 컬럼으로 User 테이블의 id와 연결
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * 댓글 내용
     * TEXT 타입으로 긴 내용도 저장 가능
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 댓글 생성 시간
     * @PrePersist에서 자동 설정됨
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 댓글 수정 시간
     * @PreUpdate에서 자동 갱신됨
     */
    @Column
    private LocalDateTime updatedAt;

    // === 계층형 댓글 구조를 위한 자기 참조 관계 ===
    
    /**
     * 부모 댓글 (대댓글인 경우에만 설정)
     * - 일반 댓글: null
     * - 대댓글: 부모 댓글의 참조
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /**
     * 자식 댓글 목록 (대댓글들)
     * - cascade = CascadeType.ALL: 부모 댓글 삭제 시 모든 대댓글도 함께 삭제
     * - @Builder.Default: 빌더 패턴 사용 시 기본값으로 빈 리스트 생성
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Comment> children = new ArrayList<>();

    // === JPA 라이프사이클 콜백 메서드들 ===
    
    /**
     * 엔티티가 데이터베이스에 저장되기 전에 자동 호출
     * 생성시간과 수정시간을 현재 시간으로 설정
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티가 데이터베이스에서 수정되기 전에 자동 호출
     * 수정시간을 현재 시간으로 갱신
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === 도메인 로직 메서드 ===
    
    /**
     * 댓글 내용을 수정하는 메서드
     * 
     * @param content 새로운 댓글 내용
     */
    public void update(String content) {
        this.content = content;
    }
    
    /**
     * 댓글 작성자가 특정 사용자와 같은지 확인하는 메서드
     * 댓글 수정/삭제 권한 체크에 사용
     * 
     * @param user 확인할 사용자
     * @return 작성자와 같으면 true, 다르면 false
     */
    public boolean isAuthor(User user) {
        return this.author != null && this.author.getId().equals(user.getId());
    }
    
    /**
     * 댓글 작성자의 닉네임을 반환하는 메서드
     * 작성자 정보가 없는 경우 "알 수 없는 사용자" 반환
     * 
     * @return 작성자 닉네임 또는 기본값
     */
    public String getAuthorNickname() {
        return this.author != null ? this.author.getNickname() : "알 수 없는 사용자";
    }
    
    /**
     * 대댓글 여부를 확인하는 메서드
     * 
     * @return 대댓글이면 true, 일반 댓글이면 false
     */
    public boolean isReply() {
        return this.parent != null;
    }
    
    /**
     * 자식 댓글(대댓글)이 있는지 확인하는 메서드
     * 
     * @return 자식 댓글이 있으면 true, 없으면 false
     */
    public boolean hasReplies() {
        return this.children != null && !this.children.isEmpty();
    }
}