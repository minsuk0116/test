package com.jobatda.back.domain.user.dto;

import com.jobatda.back.domain.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 시 클라이언트에서 전송하는 요청 데이터를 담는 DTO 클래스
 * 
 * 이 클래스는 다음과 같은 유효성 검증을 수행합니다:
 * - 필수 필드 공백/null 체크
 * - 문자열 길이 제한 검증
 * - 이메일 형식 검증
 */
public class UserRequest {
    
    /**
     * 로그인에 사용할 사용자명
     * - 필수 입력, 공백 불가
     * - 3~50자 길이 제한
     */
    @NotBlank(message = "사용자명은 필수입니다.")
    @Size(min = 3, max = 50, message = "사용자명은 3자 이상 50자 이하여야 합니다.")
    private String username;
    
    /**
     * 사용자 비밀번호
     * - 필수 입력, 공백 불가  
     * - 6자 이상 제한 (보안상 최소 길이 설정)
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    private String password;
    
    /**
     * 게시글/댓글에 표시될 닉네임
     * - 필수 입력, 공백 불가
     * - 2~20자 길이 제한
     */
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    private String nickname;
    
    /**
     * 사용자 이메일 (선택 사항)
     * - 입력시 올바른 이메일 형식이어야 함
     */
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;
    
    /**
     * 사용자 권한 (일반적으로 회원가입시 GENERAL로 설정)
     * - 관리자가 다른 사용자를 생성할 때만 다른 권한 설정 가능
     */
    private UserRole role = UserRole.GENERAL;
    
    // === 기본 생성자 ===
    public UserRequest() {}
    
    // === 전체 필드 생성자 ===
    public UserRequest(String username, String password, String nickname, String email, UserRole role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }
    
    // === Getter/Setter 메서드들 ===
    
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
}

/**
 * 로그인 시 클라이언트에서 전송하는 요청 데이터를 담는 DTO 클래스
 * 회원가입과 별도로 분리하여 필요한 필드만 포함
 */
class LoginRequest {
    
    /**
     * 로그인 사용자명
     */
    @NotBlank(message = "사용자명은 필수입니다.")
    private String username;
    
    /**
     * 로그인 비밀번호
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
    
    // === 기본 생성자 ===
    public LoginRequest() {}
    
    // === 생성자 ===
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // === Getter/Setter 메서드들 ===
    
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
}