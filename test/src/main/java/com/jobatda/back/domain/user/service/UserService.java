package com.jobatda.back.domain.user.service;

import com.jobatda.back.domain.user.dto.UserRequest;
import com.jobatda.back.domain.user.dto.UserResponse;
import com.jobatda.back.domain.user.entity.User;
import com.jobatda.back.domain.user.entity.UserRole;
import com.jobatda.back.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 
 * 주요 기능:
 * - 회원가입 처리 (중복 검증, 비밀번호 암호화)
 * - 로그인 인증 처리
 * - 사용자 정보 조회/수정
 * - 권한 관리
 * - 계정 활성화/비활성화
 */
@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 설정
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 생성자 의존성 주입
     * 
     * @param userRepository 사용자 데이터 접근 객체
     * @param passwordEncoder 비밀번호 암호화 객체
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // === 회원가입 관련 메서드들 ===
    
    /**
     * 새 사용자 회원가입 처리
     * 
     * 처리 과정:
     * 1. 중복 검증 (사용자명, 닉네임, 이메일)
     * 2. 비밀번호 암호화
     * 3. 사용자 엔티티 생성 및 저장
     * 4. UserResponse DTO로 변환하여 반환
     * 
     * @param userRequest 회원가입 요청 데이터
     * @return 생성된 사용자 정보 (UserResponse)
     * @throws RuntimeException 중복된 사용자명/닉네임/이메일인 경우
     */
    @Transactional // 쓰기 작업이므로 트랜잭션 설정
    public UserResponse signUp(UserRequest userRequest) {
        // 1. 중복 검증
        validateDuplicateUser(userRequest);
        
        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        
        // 3. User 엔티티 생성
        User user = new User(
            userRequest.getUsername(),
            encodedPassword,
            userRequest.getNickname(),
            userRequest.getEmail(),
            userRequest.getRole()
        );
        
        // 4. 데이터베이스에 저장
        User savedUser = userRepository.save(user);
        
        // 5. UserResponse로 변환하여 반환
        return UserResponse.from(savedUser);
    }
    
    /**
     * 중복 사용자 정보 검증
     * 사용자명, 닉네임, 이메일의 중복을 체크
     * 
     * @param userRequest 검증할 사용자 정보
     * @throws RuntimeException 중복된 정보가 있는 경우
     */
    private void validateDuplicateUser(UserRequest userRequest) {
        // 사용자명 중복 체크
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자명입니다: " + userRequest.getUsername());
        }
        
        // 닉네임 중복 체크
        if (userRepository.existsByNickname(userRequest.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다: " + userRequest.getNickname());
        }
        
        // 이메일 중복 체크 (이메일이 제공된 경우만)
        if (userRequest.getEmail() != null && !userRequest.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(userRequest.getEmail())) {
                throw new RuntimeException("이미 존재하는 이메일입니다: " + userRequest.getEmail());
            }
        }
    }
    
    // === 로그인 관련 메서드들 ===
    
    /**
     * 사용자명과 비밀번호로 로그인 인증
     * 
     * @param username 사용자명
     * @param password 원본 비밀번호
     * @return 인증 성공한 사용자 정보 (Optional)
     */
    public Optional<UserResponse> authenticate(String username, String password) {
        // 1. 사용자명으로 사용자 조회
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isEmpty()) {
            return Optional.empty(); // 사용자를 찾을 수 없음
        }
        
        User user = userOptional.get();
        
        // 2. 계정 활성화 상태 확인
        if (!user.getEnabled()) {
            return Optional.empty(); // 비활성화된 계정
        }
        
        // 3. 비밀번호 검증
        if (passwordEncoder.matches(password, user.getPassword())) {
            return Optional.of(UserResponse.from(user));
        }
        
        return Optional.empty(); // 비밀번호 불일치
    }
    
    // === 사용자 조회 메서드들 ===
    
    /**
     * 모든 사용자 목록 조회 (관리자용)
     * 
     * @return 모든 사용자들의 UserResponse 리스트
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 ID로 사용자 조회
     * 
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보 (Optional)
     */
    public Optional<UserResponse> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from);
    }
    
    /**
     * 사용자명으로 사용자 조회
     * 
     * @param username 조회할 사용자명
     * @return 사용자 정보 (Optional)
     */
    public Optional<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserResponse::from);
    }
    
    /**
     * 닉네임으로 사용자 조회
     * 
     * @param nickname 조회할 닉네임
     * @return 사용자 정보 (Optional)
     */
    public Optional<UserResponse> getUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .map(UserResponse::from);
    }
    
    /**
     * 권한별 사용자 목록 조회
     * 
     * @param role 조회할 권한
     * @return 해당 권한을 가진 사용자들의 리스트
     */
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
    
    // === 사용자 정보 수정 메서드들 ===
    
    /**
     * 사용자 정보 수정
     * 
     * @param userId 수정할 사용자 ID
     * @param userRequest 수정할 정보
     * @return 수정된 사용자 정보
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserRequest userRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        // 수정 가능한 필드들 업데이트
        user.setNickname(userRequest.getNickname());
        user.setEmail(userRequest.getEmail());
        
        // 비밀번호가 제공된 경우에만 업데이트
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        return UserResponse.from(updatedUser);
    }
    
    /**
     * 사용자 권한 변경 (관리자 전용)
     * 
     * @param userId 권한을 변경할 사용자 ID
     * @param newRole 새로운 권한
     * @return 수정된 사용자 정보
     */
    @Transactional
    public UserResponse changeUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        return UserResponse.from(updatedUser);
    }
    
    /**
     * 계정 활성화/비활성화 (관리자 전용)
     * 
     * @param userId 상태를 변경할 사용자 ID
     * @param enabled 활성화 상태 (true: 활성, false: 비활성)
     * @return 수정된 사용자 정보
     */
    @Transactional
    public UserResponse toggleUserStatus(Long userId, Boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        return UserResponse.from(updatedUser);
    }
    
    // === 중복 체크 메서드들 (실시간 검증용) ===
    
    /**
     * 사용자명 중복 체크
     * 
     * @param username 확인할 사용자명
     * @return 중복되면 true, 사용 가능하면 false
     */
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 닉네임 중복 체크
     * 
     * @param nickname 확인할 닉네임
     * @return 중복되면 true, 사용 가능하면 false
     */
    public boolean isNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 닉네임 중복 체크 (특정 사용자 제외)
     * 닉네임 변경 시 자신을 제외하고 중복을 확인합니다.
     * 
     * @param nickname 확인할 닉네임
     * @param userId 제외할 사용자 ID (본인)
     * @return 중복되면 true, 사용 가능하면 false
     */
    public boolean isNicknameExistsExcludeUser(String nickname, Long userId) {
        return userRepository.existsByNicknameAndIdNot(nickname, userId);
    }
    
    /**
     * 이메일 중복 체크
     * 
     * @param email 확인할 이메일
     * @return 중복되면 true, 사용 가능하면 false
     */
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // === 통계 메서드들 ===
    
    /**
     * 권한별 사용자 수 조회
     * 
     * @param role 권한
     * @return 해당 권한을 가진 사용자 수
     */
    public long countUsersByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
    
    /**
     * 전체 활성 사용자 수 조회
     * 
     * @return 활성화된 사용자의 총 수
     */
    public long countActiveUsers() {
        return userRepository.countByEnabled(true);
    }

    // === 닉네임 변경 관련 메서드 ===

    /**
     * 사용자 닉네임 변경
     * 
     * @param userId 닉네임을 변경할 사용자 ID
     * @param newNickname 새로운 닉네임
     * @return 변경된 사용자 정보
     * @throws RuntimeException 사용자를 찾을 수 없거나 중복된 닉네임인 경우
     */
    @Transactional
    public UserResponse changeUserNickname(Long userId, String newNickname) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 닉네임 중복 확인 (자신 제외)
        if (isNicknameExistsExcludeUser(newNickname, userId)) {
            throw new RuntimeException("이미 사용중인 닉네임입니다.");
        }
        
        // 닉네임 변경
        user.setNickname(newNickname);
        User savedUser = userRepository.save(user);
        
        return UserResponse.from(savedUser);
    }
}