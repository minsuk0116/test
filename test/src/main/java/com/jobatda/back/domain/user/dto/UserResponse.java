package com.jobatda.back.domain.user.dto;

import com.jobatda.back.domain.user.entity.User;
import com.jobatda.back.domain.user.entity.UserRole;

import java.time.LocalDateTime;

/**
 * 클라이언트에게 사용자 정보를 전송할 때 사용하는 응답 DTO 클래스
 * 
 * 보안상 중요한 정보(비밀번호 등)는 제외하고 필요한 정보만 포함합니다:
 * - 사용자 ID, 사용자명, 닉네임
 * - 권한 정보  
 * - 계정 상태
 * - 생성/수정 일시
 */
public class UserResponse {
    
    /**
     * 사용자 고유 식별자
     */
    private Long id;
    
    /**
     * 로그인 사용자명
     */
    private String username;
    
    /**
     * 표시용 닉네임
     */
    private String nickname;
    
    /**
     * 사용자 이메일 (선택적)
     */
    private String email;
    
    /**
     * 사용자 권한 (ADMIN, COMPANY, GENERAL)
     */
    private UserRole role;
    
    /**
     * 계정 활성화 상태
     */
    private Boolean enabled;
    
    /**
     * 계정 생성 일시
     */
    private LocalDateTime createdAt;
    
    /**
     * 계정 정보 최종 수정 일시
     */
    private LocalDateTime updatedAt;
    
    // === 기본 생성자 ===
    public UserResponse() {}
    
    // === User 엔티티로부터 UserResponse 생성하는 생성자 ===
    /**
     * User 엔티티 객체를 받아서 UserResponse DTO로 변환하는 생성자
     * 비밀번호 등 민감한 정보는 제외하고 변환
     * 
     * @param user 변환할 User 엔티티 객체
     */
    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.enabled = user.getEnabled();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
    
    // === Getter/Setter 메서드들 ===
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // === 정적 팩토리 메서드 ===
    /**
     * User 엔티티를 UserResponse DTO로 변환하는 정적 팩토리 메서드
     * 
     * @param user 변환할 User 엔티티
     * @return UserResponse DTO 객체
     */
    public static UserResponse from(User user) {
        return new UserResponse(user);
    }
}

/**
 * 로그인 성공시 클라이언트에게 전송하는 응답 DTO
 * JWT 토큰과 사용자 정보를 포함
 */
class LoginResponse {
    
    /**
     * JWT 인증 토큰
     */
    private String token;
    
    /**
     * 토큰 타입 (일반적으로 "Bearer")
     */
    private String tokenType = "Bearer";
    
    /**
     * 로그인한 사용자 정보
     */
    private UserResponse user;
    
    // === 기본 생성자 ===
    public LoginResponse() {}
    
    // === 생성자 ===
    public LoginResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }
    
    // === Getter/Setter 메서드들 ===
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public UserResponse getUser() {
        return user;
    }
    
    public void setUser(UserResponse user) {
        this.user = user;
    }
}