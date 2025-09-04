package com.jobatda.back.domain.user.repository;

import com.jobatda.back.domain.user.entity.User;
import com.jobatda.back.domain.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User 엔티티에 대한 데이터 접근을 담당하는 Repository 인터페이스
 * 
 * Spring Data JPA를 사용하여 다음과 같은 기능을 제공합니다:
 * - 기본 CRUD 연산 (생성, 조회, 수정, 삭제)
 * - 사용자명 및 닉네임 기반 검색
 * - 권한별 사용자 조회
 * - 계정 활성화 상태 기반 조회
 * - 중복 확인 메서드들
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 사용자명으로 사용자 조회
     * 로그인 인증시 주로 사용되는 메서드
     * 
     * @param username 조회할 사용자명
     * @return Optional로 감싼 User 객체 (없으면 Optional.empty())
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 닉네임으로 사용자 조회
     * 닉네임 중복 체크시 사용
     * 
     * @param nickname 조회할 닉네임
     * @return Optional로 감싼 User 객체 (없으면 Optional.empty())
     */
    Optional<User> findByNickname(String nickname);
    
    /**
     * 이메일로 사용자 조회
     * 이메일 중복 체크 및 계정 복구시 사용
     * 
     * @param email 조회할 이메일 주소
     * @return Optional로 감싼 User 객체 (없으면 Optional.empty())
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 권한별 사용자 목록 조회
     * 관리자 페이지에서 권한별로 사용자를 필터링할 때 사용
     * 
     * @param role 조회할 사용자 권한
     * @return 해당 권한을 가진 사용자들의 리스트
     */
    List<User> findByRole(UserRole role);
    
    /**
     * 계정 활성화 상태별 사용자 조회
     * 활성 사용자만 조회하거나 비활성 계정 관리시 사용
     * 
     * @param enabled 활성화 상태 (true: 활성, false: 비활성)
     * @return 해당 상태의 사용자들의 리스트
     */
    List<User> findByEnabled(Boolean enabled);
    
    /**
     * 특정 권한이면서 활성화된 사용자들 조회
     * 권한별 알림 전송이나 통계 조회시 사용
     * 
     * @param role 사용자 권한
     * @param enabled 활성화 상태
     * @return 조건에 맞는 사용자들의 리스트
     */
    List<User> findByRoleAndEnabled(UserRole role, Boolean enabled);
    
    // === 존재 여부 확인 메서드들 (중복 체크용) ===
    
    /**
     * 사용자명 존재 여부 확인
     * 회원가입시 사용자명 중복 체크용
     * 
     * @param username 확인할 사용자명
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByUsername(String username);
    
    /**
     * 닉네임 존재 여부 확인
     * 회원가입시 닉네임 중복 체크용
     * 
     * @param nickname 확인할 닉네임
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByNickname(String nickname);
    
    /**
     * 이메일 존재 여부 확인
     * 회원가입시 이메일 중복 체크용
     * 
     * @param email 확인할 이메일
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByEmail(String email);
    
    /**
     * 특정 사용자를 제외하고 닉네임 존재 여부 확인
     * 닉네임 변경시 본인을 제외한 중복 체크용
     * 
     * @param nickname 확인할 닉네임
     * @param userId 제외할 사용자 ID (본인)
     * @return 다른 사용자가 해당 닉네임을 사용중이면 true, 없으면 false
     */
    boolean existsByNicknameAndIdNot(String nickname, Long userId);
    
    // === 통계 및 카운트 메서드들 ===
    
    /**
     * 권한별 사용자 수 조회
     * 관리자 대시보드의 통계 정보 제공용
     * 
     * @param role 사용자 권한
     * @return 해당 권한을 가진 사용자의 총 수
     */
    long countByRole(UserRole role);
    
    /**
     * 활성화된 사용자 수 조회
     * 전체 활성 사용자 통계용
     * 
     * @param enabled 활성화 상태
     * @return 조건에 맞는 사용자의 총 수
     */
    long countByEnabled(Boolean enabled);
}