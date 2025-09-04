// React 핵심 라이브러리 및 훅 임포트
import React, { useEffect, useState } from "react";
// HTTP 요청을 위한 axios 라이브러리
import axios from "axios";
// 페이지 라우팅을 위한 React Router 훅
import { useNavigate } from "react-router-dom";
// 페이지네이션 컴포넌트 임포트
import Pagination from "./Pagination";
// 사용자 인증 정보를 위한 AuthContext
import { useAuth } from '../contexts/AuthContext';

/**
 * 게시판 타입 상수 정의
 * 각 게시판의 표시명(label)과 서버에서 사용하는 값(value)을 매핑
 * 순서: 공지사항 → 기업게시판 → 자유게시판 → Q&A
 */
const BOARD_TYPES = [
  { label: '공지사항', value: 'NOTICE', icon: '📢' },
  { label: '기업게시판', value: 'COMPANY', icon: '🏢' },
  { label: '자유게시판', value: 'FREE', icon: '💬' },
  { label: 'Q&A', value: 'QNA', icon: '❓' },
];

/**
 * BoardList 컴포넌트
 * 메인 페이지의 게시글 목록을 보여주는 핵심 컴포넌트
 */
function BoardList() {
  // === 상태(State) 변수들 정의 ===
  
  // 게시글 목록을 저장하는 배열 상태
  const [boards, setBoards] = useState([]);
  
  // 현재 선택된 게시판 타입 (기본값: 공지사항)
  const [selectedType, setSelectedType] = useState('NOTICE');
  
  // 현재 페이지 번호 (0부터 시작)
  const [currentPage, setCurrentPage] = useState(0);
  
  // 전체 페이지 수
  const [totalPages, setTotalPages] = useState(0);
  
  // 전체 게시글 수
  const [totalElements, setTotalElements] = useState(0);
  
  // 다음 페이지 존재 여부
  const [hasNext, setHasNext] = useState(false);
  
  // 이전 페이지 존재 여부
  const [hasPrevious, setHasPrevious] = useState(false);
  
  // 데이터 로딩 상태 (로딩 스피너 표시용)
  const [loading, setLoading] = useState(false);
  
  // 각 게시판별 게시글 수 통계 저장
  const [boardStats, setBoardStats] = useState({});
  
  // 페이지 이동을 위한 React Router 훅
  const navigate = useNavigate();
  
  // 사용자 인증 정보
  const { user } = useAuth();

  /**
   * 사용자 권한에 따라 접근 가능한 게시판 타입을 반환하는 함수
   */
  const getAvailableBoardTypes = () => {
    if (!user) {
      // 로그인하지 않은 경우 공지사항, 자유게시판, Q&A만 접근 가능
      return BOARD_TYPES.filter(type => ['NOTICE', 'FREE', 'QNA'].includes(type.value));
    }
    
    if (user.role === 'GENERAL') {
      // 일반회원: 공지사항, 자유게시판, Q&A만 접근 가능
      return BOARD_TYPES.filter(type => ['NOTICE', 'FREE', 'QNA'].includes(type.value));
    } else {
      // 회사원, 관리자: 모든 게시판 접근 가능
      return BOARD_TYPES;
    }
  };

  /**
   * useEffect 훅: 컴포넌트가 마운트되거나 의존성이 변경될 때 실행
   * [currentPage, selectedType]가 변경될 때마다 데이터를 새로 불러옴
   */
  useEffect(() => {
    fetchBoards();      // 게시글 목록 불러오기
    fetchBoardStats();  // 게시판별 통계 불러오기
  }, [currentPage, selectedType]); // 의존성 배열: 이 값들이 변경되면 useEffect 재실행

  /**
   * 게시판별 통계 데이터를 서버에서 가져오는 비동기 함수
   * 각 게시판 타입별로 몇 개의 게시글이 있는지 조회
   */
  const fetchBoardStats = async () => {
    try {
      // GET 요청으로 통계 데이터 요청
      const response = await axios.get(`http://localhost:8080/boards/stats`);
      
      // 서버 응답이 성공적일 때만 상태 업데이트
      if (response.data.success) {
        setBoardStats(response.data.boardCounts);
      }
    } catch (error) {
      // 에러 발생 시 콘솔에 로그 출력
      console.error('게시판 통계 조회 실패:', error);
    }
  };

  /**
   * 게시글 목록을 서버에서 가져오는 비동기 함수
   * 사용자 권한에 따라 필터링된 페이징 처리된 게시글 데이터를 불러옴
   */
  const fetchBoards = async () => {
    setLoading(true); // 로딩 상태 시작
    
    try {
      let response;
      
      if (user && user.id) {
        // 로그인한 사용자의 경우 권한 필터링 API 사용
        response = await axios.get(`http://localhost:8080/boards/page/secured?userId=${user.id}&page=${currentPage}&size=10`);
      } else {
        // 로그인하지 않은 경우 기본 API 사용
        response = await axios.get(`http://localhost:8080/boards/page?page=${currentPage}&size=10`);
      }
      
      const data = response.data;
      
      // 서버에서 받은 데이터로 각 상태 업데이트
      setBoards(data.boards);           // 게시글 목록
      setTotalPages(data.totalPages);   // 전체 페이지 수
      setTotalElements(data.totalElements); // 전체 게시글 수
      setHasNext(data.hasNext);         // 다음 페이지 존재 여부
      setHasPrevious(data.hasPrevious); // 이전 페이지 존재 여부
    } catch (error) {
      console.error('Error fetching boards:', error);
      // 권한 API 호출 실패 시 기본 API로 폴백
      if (user && user.id) {
        try {
          const fallbackResponse = await axios.get(`http://localhost:8080/boards/page?page=${currentPage}&size=10`);
          const data = fallbackResponse.data;
          setBoards(data.boards);
          setTotalPages(data.totalPages);
          setTotalElements(data.totalElements);
          setHasNext(data.hasNext);
          setHasPrevious(data.hasPrevious);
        } catch (fallbackError) {
          console.error('Fallback API also failed:', fallbackError);
        }
      }
    } finally {
      setLoading(false); // 성공/실패 상관없이 로딩 상태 종료
    }
  };

  /**
   * 페이지 변경 핸들러 함수
   * 페이지네이션에서 페이지를 클릭했을 때 호출됨
   */
  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  /**
   * 게시판 타입 변경 핸들러 함수
   * 게시판 타입 버튼을 클릭했을 때 호출됨
   */
  const handleTypeChange = (newType) => {
    setSelectedType(newType);
    setCurrentPage(0); // 타입 변경 시 첫 페이지로 이동
  };

  /**
   * 현재 선택된 타입의 게시글만 필터링
   * boards 배열에서 boardType이 selectedType과 일치하는 것만 반환
   */
  const filteredBoards = boards.filter(board => board.boardType === selectedType);

  /**
   * 게시글 작성 페이지로 이동하는 함수
   * 현재 선택된 게시판 타입을 쿼리 파라미터로 전달
   */
  const handleCreate = () => {
    navigate(`/boards/create?type=${selectedType}`);
  };


  // === JSX 렌더링 시작 ===
  return (
    // 전체 페이지를 감싸는 최상위 컨테이너
    <div style={{ 
      padding: '0',
      margin: '0',
      fontFamily: "'Inter', 'SF Pro Display', system-ui, -apple-system, sans-serif", // 모던한 폰트 스택
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', // 대각선 그라디언트 배경
      minHeight: '100vh', // 최소 높이를 화면 전체 높이로 설정
      position: 'relative' // 자식 요소들의 절대 위치 설정을 위한 기준점
    }}>
      {/* 배경 그라디언트 오버레이 - 고정된 배경 효과 */}
      <div style={{
        position: 'fixed', // 스크롤해도 고정
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'linear-gradient(135deg, rgba(102, 126, 234, 0.8) 0%, rgba(118, 75, 162, 0.8) 100%)',
        zIndex: -1 // 다른 요소들 뒤에 배치
      }}>
        {/* 플로팅 파티클 효과 - 미세한 원형 패턴으로 배경에 깊이감 추가 */}
        <div style={{
          position: 'absolute',
          width: '100%',
          height: '100%',
          // 여러 개의 원형 그라디언트를 중첩해서 파티클 효과 생성
          backgroundImage: `
            radial-gradient(circle at 25% 25%, rgba(255,255,255,0.1) 0%, transparent 50%),
            radial-gradient(circle at 75% 75%, rgba(255,255,255,0.05) 0%, transparent 50%),
            radial-gradient(circle at 50% 50%, rgba(255,255,255,0.03) 0%, transparent 50%)
          `
        }} />
      </div>

      {/* 메인 콘텐츠 컨테이너 - 최대 너비 제한 및 중앙 정렬 */}
      <div style={{ 
        maxWidth: '1200px',  // 최대 너비를 1200px로 제한
        margin: '0 auto',    // 좌우 중앙 정렬
        padding: '40px 20px', // 위아래 40px, 좌우 20px 패딩
        position: 'relative', // z-index 적용을 위한 상대 위치
        zIndex: 1            // 배경 위에 표시하기 위해 z-index 설정
      }}>
        {/* 헤더 섹션 - 메인 타이틀과 설명을 포함하는 카드 */}
        <div style={{ 
          background: 'rgba(255, 255, 255, 0.95)', // 95% 불투명도의 흰색 배경
          backdropFilter: 'blur(20px)',             // 배경 블러 효과 (유리 효과)
          borderRadius: '24px',                     // 둥근 모서리
          padding: '40px',                          // 내부 여백
          marginBottom: '32px',                     // 아래쪽 마진
          boxShadow: '0 20px 40px rgba(0,0,0,0.1), 0 8px 16px rgba(0,0,0,0.1)', // 이중 그림자 효과
          border: '1px solid rgba(255,255,255,0.2)' // 미세한 테두리
        }}>

          {/* 메인 타이틀 섹션 */}
          <div style={{ textAlign: 'center' }}>
            {/* 그라디언트 텍스트 타이틀 */}
            <div style={{
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', // 그라디언트 배경
              WebkitBackgroundClip: 'text',          // 웹킷 브라우저에서 텍스트에 배경 적용
              WebkitTextFillColor: 'transparent',    // 텍스트 색상을 투명하게 (그라디언트가 보이도록)
              backgroundClip: 'text',                // 표준 속성
              fontSize: '48px',                      // 큰 폰트 크기
              fontWeight: '800',                     // 매우 굵은 글씨
              margin: '0',                          // 마진 제거
              letterSpacing: '-0.02em'              // 글자 간격 조정
            }}>
              ✨ 커뮤니티
            </div>
            {/* 부제목/설명 텍스트 */}
            <p style={{
              fontSize: '18px',      // 중간 크기 폰트
              color: '#6c757d',      // 회색 색상
              margin: '16px 0 0 0',  // 위쪽만 16px 마진
              fontWeight: '400'      // 보통 굵기
            }}>
              AI와 함께하는 차세대 커뮤니티 플랫폼
            </p>
          </div>
        </div>
      
        {/* 게시판 타입 선택 */}
        <div style={{ 
          marginBottom: '32px', 
          textAlign: 'center'
        }}>
          <div style={{
            display: 'inline-flex',
            background: 'rgba(255, 255, 255, 0.9)',
            backdropFilter: 'blur(15px)',
            borderRadius: '20px',
            padding: '8px',
            boxShadow: '0 12px 24px rgba(0,0,0,0.1)',
            border: '1px solid rgba(255,255,255,0.3)'
          }}>
            {getAvailableBoardTypes().map((type, index) => {
              const count = boardStats[type.value] || 0;
              return (
                <button
                  key={type.value}
                  onClick={() => handleTypeChange(type.value)}
                  style={{
                    margin: '0 4px',
                    padding: '16px 32px',
                    border: 'none',
                    borderRadius: '16px',
                    background: selectedType === type.value 
                      ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' 
                      : 'transparent',
                    color: selectedType === type.value ? 'white' : '#495057',
                    fontWeight: selectedType === type.value ? '700' : '600',
                    cursor: 'pointer',
                    fontSize: '16px',
                    transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
                    boxShadow: selectedType === type.value 
                      ? '0 8px 20px rgba(102, 126, 234, 0.4), 0 3px 6px rgba(0,0,0,0.1)' 
                      : 'none',
                    transform: selectedType === type.value ? 'translateY(-2px)' : 'translateY(0)',
                    fontFamily: 'inherit',
                    position: 'relative'
                  }}
                  onMouseEnter={(e) => {
                    if (selectedType !== type.value) {
                      e.target.style.backgroundColor = 'rgba(102, 126, 234, 0.1)';
                      e.target.style.transform = 'translateY(-1px)';
                      e.target.style.color = '#667eea';
                    }
                  }}
                  onMouseLeave={(e) => {
                    if (selectedType !== type.value) {
                      e.target.style.backgroundColor = 'transparent';
                      e.target.style.transform = 'translateY(0)';
                      e.target.style.color = '#495057';
                    }
                  }}
                >
                  <div>{type.label}</div>
                  <div style={{
                    fontSize: '12px',
                    fontWeight: '600',
                    marginTop: '4px',
                    opacity: 0.8
                  }}>
                    {count}개
                  </div>
                </button>
              );
            })}
          </div>
        </div>


        {/* 게시글 목록 */}
        <div style={{ 
          background: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          borderRadius: '20px',
          overflow: 'hidden',
          boxShadow: '0 16px 32px rgba(0,0,0,0.1), 0 4px 8px rgba(0,0,0,0.05)',
          marginBottom: '32px',
          border: '1px solid rgba(255,255,255,0.2)'
        }}>
          {loading ? (
            <div style={{ 
              padding: '80px 40px', 
              textAlign: 'center'
            }}>
              <div style={{ 
                fontSize: '64px', 
                marginBottom: '24px',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                backgroundClip: 'text'
              }}>⏳</div>
              <div style={{
                fontSize: '18px',
                color: '#667eea',
                fontWeight: '600'
              }}>AI가 컨텐츠를 로딩하고 있습니다...</div>
            </div>
          ) : filteredBoards.length === 0 ? (
            <div style={{ 
              padding: '80px 40px', 
              textAlign: 'center'
            }}>
              <div style={{ 
                fontSize: '64px', 
                marginBottom: '24px',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                backgroundClip: 'text'
              }}>🚀</div>
              <div style={{
                fontSize: '20px',
                color: '#495057',
                fontWeight: '600',
                marginBottom: '8px'
              }}>새로운 시작을 기다리고 있습니다</div>
              <div style={{ 
                fontSize: '16px', 
                color: '#6c757d'
              }}>
                첫 번째 게시글을 작성해서 커뮤니티를 시작해보세요!
              </div>
            </div>
          ) : (
            <div style={{ padding: '0' }}>
              {filteredBoards.map((board, index) => {
                // 게시글 번호 계산 (페이지네이션 고려)
                const boardNumber = totalElements - (currentPage * 10) - index;
                
                return (
                <div 
                  key={board.id}
                  style={{ 
                    borderBottom: index < filteredBoards.length - 1 ? '1px solid rgba(0,0,0,0.06)' : 'none',
                    padding: '24px 32px',
                    background: 'transparent',
                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                    cursor: 'pointer',
                    position: 'relative'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.background = 'linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%)';
                    e.currentTarget.style.transform = 'translateY(-2px)';
                    e.currentTarget.style.boxShadow = '0 8px 25px rgba(102, 126, 234, 0.15)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.background = 'transparent';
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = 'none';
                  }}
                  onClick={() => navigate(`/boards/${board.id}`)}
                >
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'flex-start'
                  }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ 
                        color: '#2c3e50', 
                        fontSize: '18px',
                        fontWeight: '700',
                        lineHeight: '1.4',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '12px',
                        marginBottom: '4px'
                      }}>
                        <span style={{
                          fontSize: '14px',
                          color: '#999',
                          fontWeight: '500',
                          minWidth: '30px'
                        }}>{boardNumber}.</span>
                        {board.title}
                        {board.imageUrl && (
                          <span style={{ 
                            fontSize: '14px',
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            color: 'white',
                            padding: '4px 8px',
                            borderRadius: '8px',
                            fontWeight: '600'
                          }}>🎨 AI</span>
                        )}
                      </div>
                      {/* 작성자 닉네임 표시 */}
                      <div style={{
                        fontSize: '12px',
                        color: '#6c757d',
                        marginBottom: '8px',
                        fontWeight: '500'
                      }}>
                        작성자: {board.authorNickname || '알 수 없는 사용자'}
                      </div>
                      <div style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        marginTop: '12px'
                      }}>
                        <div style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          background: 'rgba(102, 126, 234, 0.1)',
                          color: '#667eea',
                          padding: '6px 12px',
                          borderRadius: '20px',
                          fontSize: '12px',
                          fontWeight: '600'
                        }}>
                          {board.boardType === 'FREE' ? '🗣️ 자유게시판' : 
                           board.boardType === 'NOTICE' ? '📢 공지사항' : 
                           board.boardType === 'COMPANY' ? '🏢 기업게시판' : '❓ Q&A'}
                        </div>
                        <div style={{ 
                          display: 'flex', 
                          gap: '16px',
                          alignItems: 'center'
                        }}>
                          <div style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                            color: '#e74c3c',
                            fontSize: '14px',
                            fontWeight: '600'
                          }}>
                            💖 {board.likeCount || 0}
                          </div>
                          <div style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                            color: '#667eea',
                            fontSize: '14px',
                            fontWeight: '600'
                          }}>
                            💬 {board.commentCount || 0}
                          </div>
                        </div>
                      </div>
                    </div>
                    {/* AI 이미지 썸네일 */}
                    {board.imageUrl && (
                      <div style={{ marginLeft: '24px' }}>
                        <img 
                          src={board.imageUrl} 
                          alt="AI Generated Thumbnail" 
                          style={{ 
                            width: '80px', 
                            height: '60px', 
                            objectFit: 'cover',
                            borderRadius: '12px',
                            border: '2px solid rgba(102, 126, 234, 0.2)',
                            boxShadow: '0 4px 12px rgba(102, 126, 234, 0.1)'
                          }} 
                        />
                      </div>
                    )}
                  </div>
                </div>
                );
              })}
            </div>
          )}
        </div>

        {/* 페이지네이션 */}
        <div style={{ marginBottom: '40px' }}>
          <Pagination 
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
            hasNext={hasNext}
            hasPrevious={hasPrevious}
          />
        </div>

        {/* 글쓰기 버튼 */}
        <div style={{ textAlign: 'center' }}>
          <button 
            onClick={handleCreate} 
            style={{ 
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              padding: '20px 40px',
              border: 'none',
              borderRadius: '50px',
              cursor: 'pointer',
              fontSize: '18px',
              fontWeight: '700',
              boxShadow: '0 12px 30px rgba(102, 126, 234, 0.4), 0 4px 8px rgba(0,0,0,0.1)',
              transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
              position: 'relative',
              overflow: 'hidden'
            }}
            onMouseEnter={(e) => {
              e.target.style.transform = 'translateY(-4px) scale(1.05)';
              e.target.style.boxShadow = '0 20px 40px rgba(102, 126, 234, 0.5), 0 8px 16px rgba(0,0,0,0.15)';
            }}
            onMouseLeave={(e) => {
              e.target.style.transform = 'translateY(0) scale(1)';
              e.target.style.boxShadow = '0 12px 30px rgba(102, 126, 234, 0.4), 0 4px 8px rgba(0,0,0,0.1)';
            }}
          >
            <span style={{ marginRight: '12px' }}>✨</span>
            새로운 이야기 시작하기
            <span style={{ marginLeft: '12px' }}>🚀</span>
          </button>
        </div>
      </div>
    </div>
  );
}

export default BoardList;
