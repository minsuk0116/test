package com.jobatda.back.domain.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 정보를 저장하는 엔티티 클래스
 * 
 * 이 클래스는 다음과 같은 사용자 정보를 관리합니다:
 * - 기본 인증 정보 (사용자명, 비밀번호)
 * - 표시용 정보 (닉네임)  
 * - 권한 정보 (사용자 역할)
 * - 계정 상태 정보 (활성화 여부)
 * - 타임스탬프 (생성일시, 수정일시)
 */
@Entity
@Table(name = "users") // 'user'는 데이터베이스 예약어이므로 'users'로 테이블명 설정
public class User {
    
    /**
     * 사용자의 고유 식별자
     * 데이터베이스에서 자동으로 증가하는 기본 키
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 로그인에 사용되는 사용자명
     * - 중복 불가 (unique = true)
     * - 필수 입력 (nullable = false)
     * - 최대 50자 제한
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    /**
     * 사용자 비밀번호
     * - 암호화되어 저장됨 (BCrypt 해시 사용 예정)
     * - 필수 입력 (nullable = false)
     */
    @Column(nullable = false)
    private String password;
    
    /**
     * 게시글과 댓글에 표시될 닉네임
     * - 중복 불가 (unique = true)
     * - 필수 입력 (nullable = false)  
     * - 최대 20자 제한
     */
    @Column(unique = true, nullable = false, length = 20)
    private String nickname;
    
    /**
     * 사용자 이메일 (선택 사항)
     * - 중복 불가 (unique = true)
     * - 계정 복구나 알림 전송에 활용
     */
    @Column(unique = true)
    private String email;
    
    /**
     * 사용자 권한
     * - ADMIN: 관리자 권한
     * - COMPANY: 회사원 권한  
     * - GENERAL: 일반 사용자 권한
     * - 기본값: GENERAL
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.GENERAL;
    
    /**
     * 계정 활성화 상태
     * - true: 활성화된 계정 (로그인 가능)
     * - false: 비활성화된 계정 (로그인 불가)
     * - 기본값: true
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    /**
     * 계정 생성 일시
     * - 자동으로 현재 시간이 설정됨
     * - 한 번 설정되면 변경되지 않음
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 계정 정보 최종 수정 일시
     * - 엔티티가 업데이트될 때마다 자동으로 현재 시간으로 갱신됨
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // === 기본 생성자 (JPA에서 필수) ===
    protected User() {}
    
    // === 생성자 (필수 필드만 포함) ===
    /**
     * 사용자 엔티티 생성자
     * 
     * @param username 로그인용 사용자명
     * @param password 암호화된 비밀번호
     * @param nickname 표시용 닉네임
     * @param email 사용자 이메일 (선택)
     * @param role 사용자 권한
     */
    public User(String username, String password, String nickname, String email, UserRole role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }
    
    // === Getter/Setter 메서드들 ===
    
    public Long getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // === 비즈니스 로직 메서드들 ===
    
    /**
     * 관리자 권한 확인
     * @return 관리자인 경우 true
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
    
    /**
     * 회사원 권한 확인  
     * @return 회사원인 경우 true
     */
    public boolean isCompanyUser() {
        return this.role == UserRole.COMPANY;
    }
    
    /**
     * 일반 사용자 권한 확인
     * @return 일반 사용자인 경우 true
     */
    public boolean isGeneralUser() {
        return this.role == UserRole.GENERAL;
    }
    
    /**
     * 기업게시판 접근 권한 확인
     * @return 기업게시판 접근 가능한 경우 true (ADMIN, COMPANY만 가능)
     */
    public boolean canAccessCompanyBoard() {
        return this.role == UserRole.ADMIN || this.role == UserRole.COMPANY;
    }
    
    /**
     * 공지사항 작성 권한 확인
     * @return 공지사항 작성 가능한 경우 true (ADMIN만 가능)
     */
    public boolean canWriteNotice() {
        return this.role == UserRole.ADMIN;
    }
}