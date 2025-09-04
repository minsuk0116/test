package com.jobatda.back.domain.user.controller;

import com.jobatda.back.domain.user.dto.UserRequest;
import com.jobatda.back.domain.user.dto.UserResponse;
import com.jobatda.back.domain.user.entity.UserRole;
import com.jobatda.back.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 사용자 관련 HTTP 요청을 처리하는 REST Controller 클래스
 * 
 * 주요 API 엔드포인트:
 * - POST /api/users/signup : 회원가입
 * - POST /api/users/login : 로그인
 * - GET /api/users : 전체 사용자 목록 조회 (관리자용)
 * - GET /api/users/{id} : 특정 사용자 조회
 * - PUT /api/users/{id} : 사용자 정보 수정
 * - POST /api/users/check-username : 사용자명 중복 체크
 * - POST /api/users/check-nickname : 닉네임 중복 체크
 * - POST /api/users/check-email : 이메일 중복 체크
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000") // 프론트엔드 CORS 허용
public class UserController {
    
    private final UserService userService;
    
    /**
     * 생성자 의존성 주입
     * 
     * @param userService 사용자 서비스 객체
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    // === 회원가입/로그인 관련 엔드포인트 ===
    
    /**
     * 회원가입 API
     * 새로운 사용자 계정을 생성합니다.
     * 
     * @param userRequest 회원가입 요청 데이터 (사용자명, 비밀번호, 닉네임, 이메일, 권한)
     * @return 201 Created: 생성된 사용자 정보
     *         400 Bad Request: 유효성 검증 실패 또는 중복된 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserRequest userRequest) {
        try {
            UserResponse newUser = userService.signUp(userRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (RuntimeException e) {
            // 중복된 정보나 유효성 검증 실패시 에러 응답
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 로그인 API
     * 사용자명과 비밀번호로 인증을 수행합니다.
     * 
     * @param loginRequest 로그인 요청 데이터 (사용자명, 비밀번호)
     * @return 200 OK: 인증 성공시 사용자 정보
     *         401 Unauthorized: 인증 실패
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        // 입력 값 검증
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "사용자명과 비밀번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(error);
        }
        
        // 인증 처리
        Optional<UserResponse> authenticatedUser = userService.authenticate(username, password);
        
        if (authenticatedUser.isPresent()) {
            // 로그인 성공 응답 (실제로는 JWT 토큰을 포함해야 함)
            Map<String, Object> response = new HashMap<>();
            response.put("message", "로그인 성공");
            response.put("user", authenticatedUser.get());
            // TODO: JWT 토큰 생성 및 추가
            // response.put("token", jwtToken);
            
            return ResponseEntity.ok(response);
        } else {
            // 로그인 실패 응답
            Map<String, String> error = new HashMap<>();
            error.put("error", "잘못된 사용자명 또는 비밀번호입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    // === 사용자 조회 관련 엔드포인트 ===
    
    /**
     * 전체 사용자 목록 조회 API (관리자용)
     * 
     * @return 200 OK: 전체 사용자 목록
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * 특정 사용자 조회 API
     * 
     * @param id 조회할 사용자 ID
     * @return 200 OK: 사용자 정보
     *         404 Not Found: 사용자를 찾을 수 없음
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<UserResponse> user = userService.getUserById(id);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "사용자를 찾을 수 없습니다.");
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 권한별 사용자 목록 조회 API
     * 
     * @param role 조회할 권한 (ADMIN, COMPANY, GENERAL)
     * @return 200 OK: 해당 권한을 가진 사용자 목록
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    // === 사용자 정보 수정 관련 엔드포인트 ===
    
    /**
     * 사용자 정보 수정 API
     * 
     * @param id 수정할 사용자 ID
     * @param userRequest 수정할 정보
     * @return 200 OK: 수정된 사용자 정보
     *         404 Not Found: 사용자를 찾을 수 없음
     *         400 Bad Request: 유효성 검증 실패
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, 
                                      @Valid @RequestBody UserRequest userRequest) {
        try {
            UserResponse updatedUser = userService.updateUser(id, userRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 사용자 권한 변경 API (관리자용)
     * 
     * @param id 권한을 변경할 사용자 ID
     * @param roleRequest 새로운 권한 정보
     * @return 200 OK: 수정된 사용자 정보
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, 
                                          @RequestBody Map<String, UserRole> roleRequest) {
        try {
            UserRole newRole = roleRequest.get("role");
            UserResponse updatedUser = userService.changeUserRole(id, newRole);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 계정 활성화/비활성화 API (관리자용)
     * 
     * @param id 상태를 변경할 사용자 ID
     * @param statusRequest 활성화 상태 정보
     * @return 200 OK: 수정된 사용자 정보
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id, 
                                            @RequestBody Map<String, Boolean> statusRequest) {
        try {
            Boolean enabled = statusRequest.get("enabled");
            UserResponse updatedUser = userService.toggleUserStatus(id, enabled);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // === 중복 체크 관련 엔드포인트 ===
    
    /**
     * 사용자명 중복 체크 API
     * 회원가입 시 실시간으로 사용자명 중복을 확인합니다.
     * 
     * @param request 확인할 사용자명
     * @return 200 OK: 중복 여부 (exists: boolean)
     */
    @PostMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        boolean exists = userService.isUsernameExists(username);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 닉네임 중복 체크 API
     * 회원가입 시 실시간으로 닉네임 중복을 확인합니다.
     * 
     * @param request 확인할 닉네임
     * @return 200 OK: 중복 여부 (exists: boolean)
     */
    @PostMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        boolean exists = userService.isNicknameExists(nickname);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 이메일 중복 체크 API
     * 회원가입 시 실시간으로 이메일 중복을 확인합니다.
     * 
     * @param request 확인할 이메일
     * @return 200 OK: 중복 여부 (exists: boolean)
     */
    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean exists = userService.isEmailExists(email);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    // === 통계 관련 엔드포인트 ===
    
    /**
     * 사용자 통계 조회 API (관리자용)
     * 권한별 사용자 수와 활성 사용자 수를 제공합니다.
     * 
     * @return 200 OK: 통계 정보
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 권한별 사용자 수
        statistics.put("adminCount", userService.countUsersByRole(UserRole.ADMIN));
        statistics.put("companyCount", userService.countUsersByRole(UserRole.COMPANY));
        statistics.put("generalCount", userService.countUsersByRole(UserRole.GENERAL));
        
        // 전체 활성 사용자 수
        statistics.put("activeUserCount", userService.countActiveUsers());
        
        // 전체 사용자 수
        statistics.put("totalUserCount", userService.getAllUsers().size());
        
        return ResponseEntity.ok(statistics);
    }
}