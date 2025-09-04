package com.jobatda.back.domain.board.service;

// AI 이미지 생성 서비스
import com.jobatda.back.domain.ai.service.AiImageService;
// 게시글 관련 DTO들
import com.jobatda.back.domain.board.dto.BoardCreateRequest;   // 게시글 생성 요청 DTO
import com.jobatda.back.domain.board.dto.BoardPageResponse;    // 페이징된 게시글 응답 DTO
import com.jobatda.back.domain.board.dto.BoardResponse;        // 게시글 응답 DTO
import com.jobatda.back.domain.board.dto.BoardUpdateRequest;   // 게시글 수정 요청 DTO
import com.jobatda.back.domain.board.entity.Board;            // 게시글 엔티티
import com.jobatda.back.domain.board.entity.BoardType;        // 게시글 타입 열거형
import com.jobatda.back.domain.board.repository.BoardRepository; // 게시글 데이터 접근 계층
// 연관된 다른 도메인의 Repository들
import com.jobatda.back.domain.comment.repository.CommentRepository;     // 댓글 Repository
import com.jobatda.back.domain.like.repository.BoardLikeRepository;      // 좋아요 Repository
import com.jobatda.back.domain.user.entity.User;                        // 사용자 엔티티
import com.jobatda.back.domain.user.entity.UserRole;                    // 사용자 권한 열거형
import com.jobatda.back.domain.user.repository.UserRepository;           // 사용자 Repository

// Lombok - 코드 간소화
import lombok.RequiredArgsConstructor;

// Spring Data JPA - 페이징 처리
import org.springframework.data.domain.Page;         // 페이징된 결과를 담는 인터페이스
import org.springframework.data.domain.PageRequest;  // 페이징 요청 생성 클래스
import org.springframework.data.domain.Pageable;     // 페이징 정보 인터페이스
import org.springframework.data.domain.Sort;         // 정렬 정보 클래스

// Spring Framework - 서비스 계층
import org.springframework.stereotype.Service;                    // 서비스 계층임을 표시
import org.springframework.transaction.annotation.Transactional;  // 트랜잭션 처리

// Java 기본 라이브러리
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BoardService 클래스
 * 게시글 관련 비즈니스 로직을 처리하는 서비스 계층
 * 
 * Spring 3계층 아키텍처에서 Service 역할:
 * - Controller에서 받은 요청을 처리
 * - 비즈니스 로직 실행
 * - Repository를 통해 데이터 접근
 * - 트랜잭션 관리
 * 
 * 주요 기능:
 * - 게시글 CRUD (생성, 조회, 수정, 삭제)
 * - AI 이미지와 함께 게시글 생성
 * - 페이징 처리된 게시글 목록 조회
 * - 댓글 수, 좋아요 수 포함한 상세 정보 조회
 */
@Service                                    // Spring의 서비스 계층 컴포넌트로 등록
@RequiredArgsConstructor                   // final 필드들의 생성자 자동 생성 (의존성 주입용)
@Transactional(readOnly = true)           // 클래스 레벨에서 읽기 전용 트랜잭션 기본 설정
public class BoardService {

    // === 의존성 주입되는 Repository들 ===
    // final로 불변성 보장, @RequiredArgsConstructor에 의해 생성자 주입
    
    /**
     * 게시글 데이터 접근을 위한 Repository
     * JPA를 통해 데이터베이스의 board 테이블과 연동
     */
    private final BoardRepository boardRepository;
    
    /**
     * 댓글 데이터 접근을 위한 Repository
     * 게시글 조회 시 댓글 수 계산에 사용
     */
    private final CommentRepository commentRepository;
    
    /**
     * 좋아요 데이터 접근을 위한 Repository  
     * 게시글 조회 시 좋아요 수 계산에 사용
     */
    private final BoardLikeRepository boardLikeRepository;
    
    /**
     * AI 이미지 생성 서비스
     * 게시글 내용을 기반으로 관련 이미지 자동 생성
     */
    private final AiImageService aiImageService;
    
    /**
     * 사용자 데이터 접근을 위한 Repository
     * 게시글 생성 시 작성자 정보 설정에 사용
     */
    private final UserRepository userRepository;

    /**
     * 새로운 게시글을 생성하는 메서드 (작성자 정보 및 권한 체크 포함)
     * 
     * @param request 게시글 생성에 필요한 데이터 (제목, 내용, 타입 등)
     * @param authorId 작성자의 사용자 ID
     * @return 생성된 게시글 엔티티
     * 
     * @Transactional: 쓰기 작업이므로 읽기 전용이 아닌 일반 트랜잭션 적용
     * 트랜잭션 내에서 예외 발생 시 자동 롤백됨
     */
    @Transactional
    public Board createBoard(BoardCreateRequest request, Long authorId) {
        // 작성자 정보 조회
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + authorId));
        
        // 권한별 게시판 작성 권한 체크
        validateBoardWritePermission(author, request.getBoardType());
        
        // DTO를 엔티티로 변환하고 작성자 설정
        Board board = request.toEntity();
        board = Board.builder()
                .title(board.getTitle())
                .content(board.getContent())
                .boardType(board.getBoardType())
                .imageUrl(board.getImageUrl())
                .author(author)  // 작성자 정보 설정
                .build();
                
        return boardRepository.save(board);
    }

    /**
     * AI 이미지와 함께 게시글을 생성하는 메서드 (작성자 정보 및 권한 체크 포함)
     * 
     * @param request 게시글 생성 데이터
     * @param authorId 작성자의 사용자 ID
     * @param generateImage AI 이미지 생성 여부
     * @return 생성된 게시글 엔티티 (AI 이미지 URL 포함 가능)
     */
    @Transactional
    public Board createBoardWithAiImage(BoardCreateRequest request, Long authorId, boolean generateImage) {
        // 작성자 정보 조회
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + authorId));
        
        // 권한별 게시판 작성 권한 체크
        validateBoardWritePermission(author, request.getBoardType());
        
        if (generateImage) {
            // 게시글 제목과 내용을 결합하여 AI 이미지 생성 프롬프트 생성
            String prompt = request.getTitle() + " " + request.getContent();
            String imageUrl = aiImageService.generateImage(prompt);  // AI 서비스 호출
            request.setImageUrl(imageUrl);  // 생성된 이미지 URL을 요청 객체에 설정
        }
        
        // DTO를 엔티티로 변환하고 작성자 설정
        Board board = request.toEntity();
        board = Board.builder()
                .title(board.getTitle())
                .content(board.getContent())
                .boardType(board.getBoardType())
                .imageUrl(board.getImageUrl())
                .author(author)  // 작성자 정보 설정
                .build();
                
        return boardRepository.save(board);
    }

    /**
     * 모든 게시글을 조회하는 메서드
     * 
     * 주의사항: 실제 운영환경에서는 데이터가 많을 수 있으므로,
     * 페이징 처리된 getBoardsWithPaging() 메서드 사용을 권장
     * 
     * @return 모든 게시글 목록
     */
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    /**
     * 사용자 권한에 따라 접근 가능한 게시글을 조회하는 메서드
     * 
     * @param userId 조회하는 사용자 ID
     * @return 권한에 따라 필터링된 게시글 목록
     */
    public List<Board> getAllBoardsWithPermission(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        List<BoardType> allowedTypes = getAllowedBoardTypes(user);
        return boardRepository.findByBoardTypeIn(allowedTypes);
    }

    /**
     * 페이징 처리된 게시글 목록을 조회하는 메서드
     * 각 게시글에 대한 좋아요 수와 댓글 수를 함께 조회하여 완전한 정보 제공
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 한 페이지당 게시글 수
     * @return 페이징 정보와 게시글 목록이 포함된 응답 객체
     */
    public BoardPageResponse getBoardsWithPaging(int page, int size) {
        // ID 내림차순으로 정렬하여 최신 게시글부터 조회
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Board> boardPage = boardRepository.findAll(pageable);
        
        // 각 게시글에 대해 좋아요 수와 댓글 수를 조회하고 Response DTO로 변환
        List<BoardResponse> boardResponses = boardPage.getContent().stream()
                .map(board -> {
                    Long likeCount = boardLikeRepository.countByBoardId(board.getId());
                    Long commentCount = commentRepository.countByBoardId(board.getId());
                    return BoardResponse.of(board, likeCount, commentCount);
                })
                .collect(Collectors.toList());

        // Page 객체를 프론트엔드에서 사용하기 쉬운 BoardPageResponse로 변환
        return BoardPageResponse.builder()
                .boards(boardResponses)
                .currentPage(boardPage.getNumber())         // 현재 페이지 번호
                .totalPages(boardPage.getTotalPages())      // 전체 페이지 수
                .totalElements(boardPage.getTotalElements()) // 전체 게시글 수
                .size(boardPage.getSize())                  // 페이지 크기
                .first(boardPage.isFirst())                 // 첫 페이지 여부
                .last(boardPage.isLast())                   // 마지막 페이지 여부
                .hasNext(boardPage.hasNext())               // 다음 페이지 존재 여부
                .hasPrevious(boardPage.hasPrevious())       // 이전 페이지 존재 여부
                .build();
    }

    /**
     * 사용자 권한에 따라 접근 가능한 게시글을 페이징으로 조회하는 메서드
     * 
     * @param userId 조회하는 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 한 페이지당 게시글 수
     * @return 권한에 따라 필터링된 페이징 응답 객체
     */
    public BoardPageResponse getBoardsWithPagingAndPermission(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        List<BoardType> allowedTypes = getAllowedBoardTypes(user);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Board> boardPage = boardRepository.findByBoardTypeIn(allowedTypes, pageable);
        
        // 각 게시글에 대해 좋아요 수와 댓글 수를 조회하고 Response DTO로 변환
        List<BoardResponse> boardResponses = boardPage.getContent().stream()
                .map(board -> {
                    Long likeCount = boardLikeRepository.countByBoardId(board.getId());
                    Long commentCount = commentRepository.countByBoardId(board.getId());
                    return BoardResponse.of(board, likeCount, commentCount);
                })
                .collect(Collectors.toList());

        // Page 객체를 프론트엔드에서 사용하기 쉬운 BoardPageResponse로 변환
        return BoardPageResponse.builder()
                .boards(boardResponses)
                .currentPage(boardPage.getNumber())         // 현재 페이지 번호
                .totalPages(boardPage.getTotalPages())      // 전체 페이지 수
                .totalElements(boardPage.getTotalElements()) // 전체 게시글 수
                .size(boardPage.getSize())                  // 페이지 크기
                .first(boardPage.isFirst())                 // 첫 페이지 여부
                .last(boardPage.isLast())                   // 마지막 페이지 여부
                .hasNext(boardPage.hasNext())               // 다음 페이지 존재 여부
                .hasPrevious(boardPage.hasPrevious())       // 이전 페이지 존재 여부
                .build();
    }

    /**
     * ID로 특정 게시글을 조회하는 메서드
     * 
     * @param id 조회할 게시글의 ID
     * @return 조회된 게시글 엔티티
     * @throws IllegalArgumentException 해당 ID의 게시글이 존재하지 않는 경우
     */
    public Board getBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID=" + id));
    }

    /**
     * 기존 게시글의 정보를 수정하는 메서드
     * 
     * @param id 수정할 게시글의 ID
     * @param request 수정할 내용이 담긴 요청 객체
     * @return 수정된 게시글 엔티티
     * 
     * JPA의 더티 체킹(Dirty Checking) 활용:
     * - 트랜잭션 내에서 엔티티의 상태가 변경되면
     * - 트랜잭션 커밋 시점에 자동으로 UPDATE 쿼리 실행
     * - 별도의 save() 호출 불필요
     */
    @Transactional
    public Board updateBoard(Long id, BoardUpdateRequest request) {
        Board board = getBoardById(id);  // 존재하지 않으면 예외 발생
        board.update(request.getBoardType(), request.getTitle(), request.getContent()); 
        return board; // JPA의 더티체킹으로 업데이트 자동 반영
    }

    /**
     * 게시글을 삭제하는 메서드
     * 
     * @param id 삭제할 게시글의 ID
     * 
     * 주의사항:
     * - 실제 운영환경에서는 soft delete(논리적 삭제) 방식을 고려할 수 있음
     * - 연관된 댓글이나 좋아요 데이터의 처리 정책도 함께 고려 필요
     * - 현재는 JPA의 cascade 설정에 따라 연관 데이터 처리됨
     */
    @Transactional
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

    /**
     * 특정 게시판 타입의 게시글을 페이징으로 조회하는 메서드
     * 
     * @param boardType 조회할 게시판 타입 (NOTICE, COMPANY, FREE, QNA)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 한 페이지당 게시글 수
     * @return 페이징 정보와 해당 타입의 게시글 목록이 포함된 응답 객체
     * 
     * 사용 예시:
     * - 기업게시판만 조회: getBoardsByTypeWithPaging(BoardType.COMPANY, 0, 10)
     * - 공지사항만 조회: getBoardsByTypeWithPaging(BoardType.NOTICE, 0, 5)
     */
    public BoardPageResponse getBoardsByTypeWithPaging(BoardType boardType, int page, int size) {
        // ID 내림차순으로 정렬하여 최신 게시글부터 조회
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        
        // Repository에서 특정 타입의 게시글만 페이징 조회
        Page<Board> boardPage = boardRepository.findByBoardType(boardType, pageable);
        
        // 각 게시글에 대해 좋아요 수와 댓글 수를 조회하고 Response DTO로 변환
        List<BoardResponse> boardResponses = boardPage.getContent().stream()
                .map(board -> {
                    Long likeCount = boardLikeRepository.countByBoardId(board.getId());
                    Long commentCount = commentRepository.countByBoardId(board.getId());
                    return BoardResponse.of(board, likeCount, commentCount);
                })
                .collect(Collectors.toList());

        // Page 객체를 프론트엔드에서 사용하기 쉬운 BoardPageResponse로 변환
        return BoardPageResponse.builder()
                .boards(boardResponses)
                .currentPage(boardPage.getNumber())         // 현재 페이지 번호
                .totalPages(boardPage.getTotalPages())      // 전체 페이지 수
                .totalElements(boardPage.getTotalElements()) // 전체 게시글 수
                .size(boardPage.getSize())                  // 페이지 크기
                .first(boardPage.isFirst())                 // 첫 페이지 여부
                .last(boardPage.isLast())                   // 마지막 페이지 여부
                .hasNext(boardPage.hasNext())               // 다음 페이지 존재 여부
                .hasPrevious(boardPage.hasPrevious())       // 이전 페이지 존재 여부
                .build();
    }
    
    // === 권한별 접근 제어 메서드들 ===
    
    /**
     * 게시판 타입별 쓰기 권한을 검증하는 메서드
     * 
     * 권한별 접근 제어 규칙:
     * - NOTICE (공지사항): ADMIN만 작성 가능
     * - COMPANY (기업게시판): ADMIN, COMPANY만 작성 가능  
     * - FREE (자유게시판): 모든 사용자 작성 가능
     * - QNA (Q&A): 모든 사용자 작성 가능
     * 
     * @param user 작성하려는 사용자
     * @param boardType 게시판 타입
     * @throws RuntimeException 권한이 없는 경우 예외 발생
     */
    private void validateBoardWritePermission(User user, BoardType boardType) {
        switch (boardType) {
            case NOTICE:
                // 공지사항은 관리자만 작성 가능
                if (!user.canWriteNotice()) {
                    throw new RuntimeException("공지사항은 관리자만 작성할 수 있습니다.");
                }
                break;
                
            case COMPANY:
                // 기업게시판은 관리자와 회사원만 작성 가능
                if (!user.canAccessCompanyBoard()) {
                    throw new RuntimeException("기업게시판은 회사원 이상의 권한이 필요합니다.");
                }
                break;
                
            case FREE:
            case QNA:
                // 자유게시판과 Q&A는 모든 로그인 사용자가 작성 가능
                // 별도 권한 체크 없음
                break;
                
            default:
                throw new RuntimeException("지원되지 않는 게시판 타입입니다: " + boardType);
        }
    }
    
    /**
     * 게시판 타입별 읽기 권한을 검증하는 메서드
     * 
     * 권한별 접근 제어 규칙:
     * - NOTICE (공지사항): 모든 사용자 읽기 가능
     * - COMPANY (기업게시판): ADMIN, COMPANY만 읽기 가능
     * - FREE (자유게시판): 모든 사용자 읽기 가능
     * - QNA (Q&A): 모든 사용자 읽기 가능
     * 
     * @param user 조회하려는 사용자
     * @param boardType 게시판 타입
     * @throws RuntimeException 권한이 없는 경우 예외 발생
     */
    public void validateBoardReadPermission(User user, BoardType boardType) {
        switch (boardType) {
            case COMPANY:
                // 기업게시판은 관리자와 회사원만 읽기 가능
                if (!user.canAccessCompanyBoard()) {
                    throw new RuntimeException("기업게시판에 접근할 권한이 없습니다.");
                }
                break;
                
            case NOTICE:
            case FREE:
            case QNA:
                // 공지사항, 자유게시판, Q&A는 모든 사용자가 읽기 가능
                // 별도 권한 체크 없음
                break;
                
            default:
                throw new RuntimeException("지원되지 않는 게시판 타입입니다: " + boardType);
        }
    }
    
    /**
     * 특정 게시판 타입의 게시글 목록을 사용자 권한에 따라 조회하는 메서드
     * 
     * @param boardType 조회할 게시판 타입
     * @param userId 조회하는 사용자 ID  
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 권한이 있는 경우 게시글 목록, 권한이 없는 경우 예외 발생
     */
    public BoardPageResponse getBoardsByTypeWithPermissionCheck(BoardType boardType, Long userId, int page, int size) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        // 읽기 권한 체크
        validateBoardReadPermission(user, boardType);
        
        // 권한이 있으면 게시글 목록 반환
        return getBoardsByTypeWithPaging(boardType, page, size);
    }
    
    /**
     * 사용자 권한에 따라 접근 가능한 게시판 타입 목록을 반환하는 헬퍼 메서드
     * 
     * @param user 사용자 정보
     * @return 접근 가능한 게시판 타입 리스트
     */
    private List<BoardType> getAllowedBoardTypes(User user) {
        if (user.getRole() == UserRole.GENERAL) {
            // 일반회원: 공지사항, 자유게시판, Q&A만 접근 가능
            return Arrays.asList(BoardType.NOTICE, BoardType.FREE, BoardType.QNA);
        } else {
            // 회사원, 관리자: 모든 게시판 접근 가능
            return Arrays.asList(BoardType.NOTICE, BoardType.COMPANY, BoardType.FREE, BoardType.QNA);
        }
    }
}