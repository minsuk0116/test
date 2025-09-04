package com.jobatda.back.domain.board.repository;

// Spring Data JPA Repository 인터페이스
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// 게시글 엔티티
import com.jobatda.back.domain.board.entity.Board;
import com.jobatda.back.domain.board.entity.BoardType;

import java.util.List;

/**
 * BoardRepository 인터페이스
 * 게시글 데이터 접근을 담당하는 Repository 계층
 * 
 * Spring Data JPA의 JpaRepository를 상속받아 기본 CRUD 메서드 자동 제공:
 * - save(entity): 저장/수정
 * - findById(id): ID로 조회  
 * - findAll(): 전체 조회
 * - delete(entity): 삭제
 * - 페이징 및 정렬 기능 포함
 * 
 * 커스텀 쿼리 메서드가 필요한 경우 메서드명 규칙에 따라 자동 생성 가능
 * 예: findByTitle, findByBoardType 등
 */
@Repository                                    // Spring의 Repository 계층 컴포넌트로 등록
public interface BoardRepository extends JpaRepository<Board, Long> {
    // JpaRepository<Entity타입, 기본키타입>
    // Board: 관리할 엔티티 타입
    // Long: Board 엔티티의 기본키(id) 타입
    
    /**
     * 특정 게시판 타입의 게시글을 페이징으로 조회하는 메서드
     * 
     * Spring Data JPA 쿼리 메서드 네이밍 규칙:
     * - findBy + 필드명 + Pageable 파라미터 = 페이징 쿼리 자동 생성
     * - 실제 실행되는 SQL: SELECT * FROM boards WHERE board_type = ? ORDER BY ... LIMIT ... OFFSET ...
     * 
     * @param boardType 조회할 게시판 타입
     * @param pageable 페이징 정보 (페이지 번호, 사이즈, 정렬 조건)
     * @return 페이징된 게시글 목록
     */
    Page<Board> findByBoardType(BoardType boardType, Pageable pageable);

    /**
     * 사용자 권한에 따라 접근 가능한 게시글들을 페이징으로 조회
     * - 일반회원: 공지사항, 자유게시판, Q&A만
     * - 회사원/관리자: 모든 게시판
     */
    @Query("SELECT b FROM Board b WHERE b.boardType IN :allowedTypes ORDER BY b.id DESC")
    Page<Board> findByBoardTypeIn(@Param("allowedTypes") List<BoardType> allowedTypes, Pageable pageable);

    /**
     * 사용자 권한에 따라 접근 가능한 게시글들을 조회 (페이징 없음)
     */
    @Query("SELECT b FROM Board b WHERE b.boardType IN :allowedTypes ORDER BY b.id DESC")
    List<Board> findByBoardTypeIn(@Param("allowedTypes") List<BoardType> allowedTypes);
    
    // 기본 제공 메서드들로 충분한 경우 별도 메서드 선언 불필요
    // 필요시 쿼리 메서드나 @Query 어노테이션으로 커스텀 쿼리 추가 가능
    
    // 추가 가능한 커스텀 쿼리 메서드 예시:
    // List<Board> findByTitleContaining(String keyword);  // 제목에 키워드 포함된 게시글 검색
    // List<Board> findByBoardTypeOrderByIdDesc(BoardType boardType);  // 특정 타입, ID 내림차순
    // Long countByBoardType(BoardType boardType);  // 특정 타입의 게시글 수 계산
}
