package com.jobatda.back.domain.board.controller;

// Spring Boot와 관련된 임포트들
import com.jobatda.back.domain.board.dto.BoardCreateRequest;    // 게시글 생성 요청 DTO
import com.jobatda.back.domain.board.dto.BoardPageResponse;     // 페이징된 게시글 응답 DTO
import com.jobatda.back.domain.board.dto.BoardUpdateRequest;    // 게시글 수정 요청 DTO
import com.jobatda.back.domain.board.dto.BoardResponse;         // 게시글 응답 DTO
import com.jobatda.back.domain.board.entity.Board;             // 게시글 엔티티
import com.jobatda.back.domain.board.entity.BoardType;         // 게시글 타입 열거형
import com.jobatda.back.domain.board.service.BoardService;     // 게시글 비즈니스 로직 서비스

// Spring 검증 관련
import jakarta.validation.Valid;

// Lombok 어노테이션들 (코드 간소화를 위한 라이브러리)
import lombok.RequiredArgsConstructor;  // final 필드에 대한 생성자 자동 생성
import lombok.extern.slf4j.Slf4j;       // 로깅을 위한 어노테이션

// Spring Web MVC 관련
import org.springframework.web.bind.annotation.*;

// Java 기본 라이브러리들
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BoardController 클래스
 * 게시글 관련 REST API를 제공하는 컨트롤러
 * 
 * Spring MVC 패턴에서 Controller 역할:
 * - 클라이언트의 HTTP 요청을 받아서 처리
 * - 비즈니스 로직은 Service 계층에 위임
 * - 처리 결과를 JSON 형태로 응답
 * 
 * 주요 기능:
 * - 게시글 CRUD (생성, 조회, 수정, 삭제) 
 * - AI 이미지와 함께 게시글 생성
 * - 페이지네이션을 통한 게시글 목록 조회
 * - 게시판별 통계 정보 제공
 */
@RestController                    // JSON 응답을 하는 REST API 컨트롤러임을 명시
@RequestMapping("/boards")         // 모든 메서드의 URL이 /boards로 시작함을 설정
@RequiredArgsConstructor          // final 필드에 대한 의존성 주입 생성자 자동 생성
@Slf4j                           // 로깅 기능 자동 추가 (log 변수 사용 가능)
public class BoardController {

    /**
     * 게시글 비즈니스 로직을 처리하는 서비스
     * final 키워드로 불변성 보장, @RequiredArgsConstructor에 의해 생성자 주입됨
     */
    private final BoardService boardService;

    /**
     * 새로운 게시글을 생성하는 API 엔드포인트
     * 
     * HTTP POST /boards
     * 
     * @param request 게시글 생성에 필요한 데이터 (제목, 내용, 타입 등)
     * @param authorId 작성자의 사용자 ID (TODO: 추후 JWT 인증에서 자동으로 추출하도록 변경)
     * @Valid 어노테이션: 요청 데이터의 유효성 검증 (제목 필수, 길이 제한 등)
     * @RequestBody: JSON 형태의 요청 본문을 Java 객체로 변환
     * @return 생성된 게시글 정보를 담은 응답 DTO
     */
    @PostMapping
    public BoardResponse createBoard(@Valid @RequestBody BoardCreateRequest request) {
        log.info("게시판 생성 요청: 작성자 ID={}", request.getAuthorId());
        Board board = boardService.createBoard(request, request.getAuthorId());
        return BoardResponse.from(board);
    }

    /**
     * AI 이미지와 함께 게시글을 생성하는 API 엔드포인트
     * 
     * HTTP POST /boards/with-ai-image
     * 
     * @param request 게시글 생성 데이터
     * @param authorId 작성자의 사용자 ID
     * @param generateImage AI 이미지 생성 여부 (기본값: false)
     * @RequestParam: URL 쿼리 파라미터 또는 폼 데이터를 받음
     * @return 생성된 게시글 정보 (AI 이미지 URL 포함 가능)
     */
    @PostMapping("/with-ai-image")
    public BoardResponse createBoardWithAiImage(
            @Valid @RequestBody BoardCreateRequest request,
            @RequestParam(defaultValue = "false") boolean generateImage) {
        log.info("AI 이미지와 함께 게시판 생성 요청: 작성자 ID={}, generateImage={}", request.getAuthorId(), generateImage);
        Board board = boardService.createBoardWithAiImage(request, request.getAuthorId(), generateImage);
        return BoardResponse.from(board);
    }

    /**
     * 모든 게시글을 조회하는 API (기존 API 호환성 유지용)
     * 
     * HTTP GET /boards
     * 
     * 참고: 실제 운영 환경에서는 데이터가 많을 수 있으므로 
     * 페이징 처리된 /boards/page API 사용을 권장
     * 
     * @return 모든 게시글 목록
     */
    @GetMapping
    public List<BoardResponse> getAllBoards() {
        return boardService.getAllBoards().stream()        // 모든 게시글 조회
                .map(BoardResponse::from)                  // 각 엔티티를 DTO로 변환
                .collect(Collectors.toList());             // 리스트로 수집
    }

    /**
     * 페이지네이션을 적용하여 게시글을 조회하는 API
     * 
     * HTTP GET /boards/page?page=0&size=10
     * 
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 한 페이지당 게시글 수 (기본값: 10)
     * @RequestParam: URL 쿼리 파라미터로 전달받는 값
     * @return 페이징 정보가 포함된 게시글 목록 응답
     */
    @GetMapping("/page")
    public BoardPageResponse getBoardsWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return boardService.getBoardsWithPaging(page, size);
    }

    /**
     * 사용자 권한에 따라 필터링된 게시글을 페이징으로 조회하는 API
     * 
     * HTTP GET /boards/page/secured?userId={userId}&page=0&size=10
     * 
     * @param userId 조회하는 사용자 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 한 페이지당 게시글 수 (기본값: 10)
     * @return 권한에 따라 필터링된 페이징된 게시글 목록 응답
     */
    @GetMapping("/page/secured")
    public BoardPageResponse getBoardsWithPagingAndPermission(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("권한 필터링된 게시판 페이징 조회 요청: userId={}, page={}, size={}", userId, page, size);
        return boardService.getBoardsWithPagingAndPermission(userId, page, size);
    }

    /**
     * 특정 게시글을 ID로 조회하는 API
     * 
     * HTTP GET /boards/{id}
     * 
     * @param id 조회할 게시글의 ID
     * @PathVariable: URL 경로의 {id} 부분을 파라미터로 받음
     * @return 조회된 게시글 정보
     */
    @GetMapping("/{id}")
    public BoardResponse getBoardById(@PathVariable Long id) {
        return BoardResponse.from(boardService.getBoardById(id));
    }

    /**
     * 기존 게시글을 수정하는 API
     * 
     * HTTP PUT /boards/{id}
     * 
     * @param id 수정할 게시글의 ID
     * @param request 수정할 내용이 담긴 요청 데이터
     * @return 수정된 게시글 정보
     */
    @PutMapping("/{id}")
    public BoardResponse updateBoard(@PathVariable Long id,
                                     @Valid @RequestBody BoardUpdateRequest request) {
        return BoardResponse.from(boardService.updateBoard(id, request));
    }

    /**
     * 게시글을 삭제하는 API
     * 
     * HTTP DELETE /boards/{id}
     * 
     * @param id 삭제할 게시글의 ID
     * void 반환: 성공 시 204 No Content, 실패 시 예외 발생
     */
    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        log.info("게시글 삭제 완료: {}", id);
    }

    /**
     * 특정 게시판 타입의 게시글만 조회하는 API
     * 
     * HTTP GET /boards/type/{boardType}
     * 
     * @param boardType 조회할 게시판 타입 (NOTICE, COMPANY, FREE, QNA)
     * @return 해당 타입의 게시글 목록
     */
    @GetMapping("/type/{boardType}")
    public List<BoardResponse> getBoardsByType(@PathVariable BoardType boardType) {
        log.info("{}게시판 게시글 조회 요청", boardType);
        return boardService.getAllBoards().stream()
                .filter(board -> board.getBoardType() == boardType)
                .map(BoardResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 게시판 타입의 게시글을 페이징으로 조회하는 API
     * 
     * HTTP GET /boards/type/{boardType}/page?page=0&size=10
     * 
     * @param boardType 조회할 게시판 타입
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 한 페이지당 게시글 수 (기본값: 10)
     * @return 페이징 정보가 포함된 해당 타입의 게시글 목록
     */
    @GetMapping("/type/{boardType}/page")
    public BoardPageResponse getBoardsByTypeWithPaging(
            @PathVariable BoardType boardType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("{}게시판 페이징 조회 요청: page={}, size={}", boardType, page, size);
        return boardService.getBoardsByTypeWithPaging(boardType, page, size);
    }

    /**
     * 게시판별 게시글 수 통계를 제공하는 API
     * 
     * HTTP GET /boards/stats
     * 
     * 프론트엔드에서 대시보드나 메뉴에 각 게시판별 게시글 수를 표시할 때 사용
     * 
     * @return 게시판별 통계 정보 (공지사항, 기업게시판, 자유게시판, Q&A별 개수)
     */
    @GetMapping("/stats")
    public Map<String, Object> getBoardStats() {
        log.info("게시판 통계 요청");
        
        // 응답 데이터를 담을 Map 생성
        Map<String, Object> stats = new HashMap<>();
        List<Board> allBoards = boardService.getAllBoards();
        
        // Java 8 Stream API를 사용한 타입별 게시글 수 계산
        long freeCount = allBoards.stream()
                .filter(board -> board.getBoardType() == BoardType.FREE)
                .count();
        long noticeCount = allBoards.stream()
                .filter(board -> board.getBoardType() == BoardType.NOTICE)
                .count();
        long companyCount = allBoards.stream()
                .filter(board -> board.getBoardType() == BoardType.COMPANY)
                .count();
        long qnaCount = allBoards.stream()
                .filter(board -> board.getBoardType() == BoardType.QNA)
                .count();
        
        // 게시판별 개수를 Map에 저장
        Map<String, Long> boardCounts = new HashMap<>();
        boardCounts.put("NOTICE", noticeCount);    // 공지사항 (맨 위)
        boardCounts.put("COMPANY", companyCount);  // 기업게시판 (중간)
        boardCounts.put("FREE", freeCount);        // 자유게시판
        boardCounts.put("QNA", qnaCount);          // Q&A (맨 아래)
        boardCounts.put("TOTAL", (long) allBoards.size());
        
        // 최종 응답 데이터 구성
        stats.put("boardCounts", boardCounts);
        stats.put("success", true);
        
        log.info("게시판 통계: NOTICE={}, COMPANY={}, FREE={}, QNA={}, TOTAL={}", 
                noticeCount, companyCount, freeCount, qnaCount, allBoards.size());
        
        return stats;
    }
    
    /**
     * 권한별 접근 제어가 적용된 게시판 타입별 게시글 조회 API
     * 
     * HTTP GET /boards/type/{boardType}/secured
     * 
     * @param boardType 조회할 게시판 타입
     * @param userId 조회하는 사용자 ID (TODO: JWT에서 자동 추출하도록 변경 예정)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 권한이 있는 경우 게시글 목록, 권한이 없는 경우 에러 응답
     * 
     * 권한별 접근 제어:
     * - NOTICE (공지사항): 모든 사용자 조회 가능
     * - COMPANY (기업게시판): ADMIN, COMPANY 권한만 조회 가능
     * - FREE (자유게시판): 모든 사용자 조회 가능
     * - QNA (Q&A): 모든 사용자 조회 가능
     */
    @GetMapping("/type/{boardType}/secured")
    public BoardPageResponse getBoardsByTypeWithPermissionCheck(
            @PathVariable BoardType boardType,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("권한 체크 포함 {}게시판 조회 요청: userId={}, page={}, size={}", boardType, userId, page, size);
        return boardService.getBoardsByTypeWithPermissionCheck(boardType, userId, page, size);
    }
}
