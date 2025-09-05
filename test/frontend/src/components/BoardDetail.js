// React 핵심 라이브러리 및 훅 임포트
import React, { useEffect, useState } from 'react';
// React Router 훅들 - 페이지 이동과 URL 파라미터 추출용
import { useNavigate, useParams } from 'react-router-dom';
// HTTP 요청을 위한 axios 라이브러리
import axios from 'axios';
// 페이지 이동을 위한 Link 컴포넌트
import { Link } from 'react-router-dom';
// 하위 컴포넌트들 임포트
import CommentSection from './CommentSection';   // 댓글 기능
import LikeButton from './LikeButton';           // 좋아요 버튼
import SentimentAnalysis from './SentimentAnalysis'; // 감정 분석

/**
 * BoardDetail 컴포넌트
 * 개별 게시글의 상세 내용을 보여주는 페이지
 * 
 * 주요 기능:
 * - 게시글 상세 정보 표시 (제목, 내용, AI 이미지 등)
 * - 게시글 수정/삭제 기능
 * - 좋아요 기능
 * - 댓글 시스템
 * - 감정 분석 결과 표시
 */
function BoardDetail() {
  // URL 파라미터에서 게시글 ID 추출 (예: /boards/123 → id = "123")
  const { id } = useParams();
  
  // 프로그래밍적 페이지 이동을 위한 훅
  const navigate = useNavigate();

  // === 상태 변수 정의 ===
  // 게시글 데이터를 저장하는 상태 (초기값: null)
  const [board, setBoard] = useState(null);

  /**
   * useEffect: 컴포넌트 마운트 시 게시글 데이터 로딩
   * id가 변경될 때마다 새로운 게시글 데이터를 가져옴
   */
  useEffect(() => {
    // 서버에서 특정 게시글 데이터 요청
    axios.get(`http://localhost:8080/boards/${id}`)
      .then(response => setBoard(response.data)) // 성공시 상태 업데이트
      .catch(error => console.error('Error fetching board detail:', error)); // 에러 처리
  }, [id]); // 의존성 배열: id가 변경되면 재실행

  /**
   * 게시글 삭제 핸들러 함수
   * 사용자 확인 → 서버 요청 → 성공시 목록 페이지로 이동
   */
  const handleDelete = () => {
    // 사용자에게 삭제 확인 요청
    if (window.confirm('정말 이 게시글을 삭제하시겠습니까?')) {
      // DELETE 요청으로 게시글 삭제
      axios.delete(`http://localhost:8080/boards/${id}`)
        .then(() => {
          alert('게시글이 삭제되었습니다.');
          navigate('/'); // 삭제 후 게시글 목록 페이지로 이동
        })
        .catch(error => {
          console.error('게시글 삭제 실패:', error);
          alert('게시글 삭제 중 오류가 발생했습니다.');
        });
    }
  };

  // 데이터가 아직 로딩 중일 때 로딩 메시지 표시
  // 조건부 렌더링: board가 null이면 로딩 표시, 있으면 실제 컨텐츠 표시
  if (!board) return <div>Loading...</div>;

  return (
    <div style={{
      padding: '0',
      margin: '0',
      fontFamily: "'Inter', 'SF Pro Display', system-ui, -apple-system, sans-serif",
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      minHeight: '100vh',
      position: 'relative'
    }}>
      {/* 배경 그라디언트 오버레이 */}
      <div style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'linear-gradient(135deg, rgba(102, 126, 234, 0.8) 0%, rgba(118, 75, 162, 0.8) 100%)',
        zIndex: -1
      }}>
        <div style={{
          position: 'absolute',
          width: '100%',
          height: '100%',
          backgroundImage: `
            radial-gradient(circle at 25% 25%, rgba(255,255,255,0.1) 0%, transparent 50%),
            radial-gradient(circle at 75% 75%, rgba(255,255,255,0.05) 0%, transparent 50%),
            radial-gradient(circle at 50% 50%, rgba(255,255,255,0.03) 0%, transparent 50%)
          `
        }} />
      </div>

      <div style={{
        maxWidth: '900px',
        margin: '0 auto',
        padding: '40px 20px',
        position: 'relative',
        zIndex: 1
      }}>
        {/* 상단 네비게이션 */}
        <div style={{ 
          marginBottom: '32px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <button 
            onClick={() => navigate('/')}
            style={{ 
              background: 'rgba(255, 255, 255, 0.9)',
              backdropFilter: 'blur(10px)',
              color: '#667eea',
              border: '2px solid rgba(102, 126, 234, 0.2)', 
              padding: '16px 32px',
              borderRadius: '50px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: '700',
              boxShadow: '0 8px 20px rgba(0,0,0,0.1)',
              transition: 'all 0.3s ease'
            }}
            onMouseEnter={(e) => {
              e.target.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
              e.target.style.color = 'white';
              e.target.style.transform = 'translateY(-2px)';
              e.target.style.boxShadow = '0 12px 30px rgba(102, 126, 234, 0.3)';
            }}
            onMouseLeave={(e) => {
              e.target.style.background = 'rgba(255, 255, 255, 0.9)';
              e.target.style.color = '#667eea';
              e.target.style.transform = 'translateY(0)';
              e.target.style.boxShadow = '0 8px 20px rgba(0,0,0,0.1)';
            }}
          >
            ← 목록으로 돌아가기
          </button>
          
          {/* 수정/삭제 버튼들 */}
          <div style={{ 
            display: 'flex', 
            gap: '12px'
          }}>
            <Link 
              to={`/boards/edit/${board.id}`}
              style={{ 
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white', 
                padding: '12px 20px', 
                textDecoration: 'none',
                borderRadius: '25px',
                fontSize: '14px',
                fontWeight: '600',
                boxShadow: '0 4px 12px rgba(102, 126, 234, 0.3)',
                transition: 'all 0.3s ease',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
              onMouseEnter={(e) => {
                e.target.style.transform = 'translateY(-1px)';
                e.target.style.boxShadow = '0 6px 16px rgba(102, 126, 234, 0.4)';
              }}
              onMouseLeave={(e) => {
                e.target.style.transform = 'translateY(0)';
                e.target.style.boxShadow = '0 4px 12px rgba(102, 126, 234, 0.3)';
              }}
            >
              ✏️ 수정
            </Link>
            <button 
              onClick={handleDelete} 
              style={{
                background: 'rgba(231, 76, 60, 0.1)',
                color: '#e74c3c',
                padding: '12px 20px',
                border: '2px solid rgba(231, 76, 60, 0.2)',
                borderRadius: '25px',
                fontSize: '14px',
                fontWeight: '600',
                cursor: 'pointer',
                transition: 'all 0.3s ease',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
              onMouseEnter={(e) => {
                e.target.style.background = '#e74c3c';
                e.target.style.color = 'white';
                e.target.style.transform = 'translateY(-1px)';
                e.target.style.boxShadow = '0 4px 12px rgba(231, 76, 60, 0.3)';
              }}
              onMouseLeave={(e) => {
                e.target.style.background = 'rgba(231, 76, 60, 0.1)';
                e.target.style.color = '#e74c3c';
                e.target.style.transform = 'translateY(0)';
                e.target.style.boxShadow = 'none';
              }}
            >
              🗑️ 삭제
            </button>
          </div>
        </div>
        
        {/* 메인 컨텐츠 */}
        <div style={{ 
          background: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          borderRadius: '24px',
          padding: '40px',
          marginBottom: '32px',
          boxShadow: '0 20px 40px rgba(0,0,0,0.1), 0 8px 16px rgba(0,0,0,0.1)',
          border: '1px solid rgba(255,255,255,0.2)'
        }}>
          {/* 게시글 헤더 */}
          <div style={{ marginBottom: '32px', borderBottom: '2px solid rgba(102, 126, 234, 0.1)', paddingBottom: '24px' }}>
            <div style={{ 
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              marginBottom: '16px'
            }}>
              <div style={{
                display: 'inline-flex',
                alignItems: 'center',
                background: 'rgba(102, 126, 234, 0.1)',
                color: '#667eea',
                padding: '8px 16px',
                borderRadius: '20px',
                fontSize: '14px',
                fontWeight: '700'
              }}>
                {board.boardType === 'FREE' ? '🗣️ 자유게시판' : 
                 board.boardType === 'NOTICE' ? '📢 공지사항' : 
                 board.boardType === 'COMPANY' ? '🏢 기업게시판' : '❓ Q&A'}
              </div>
              {board.imageUrl && (
                <div style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: 'white',
                  padding: '8px 16px',
                  borderRadius: '20px',
                  fontSize: '14px',
                  fontWeight: '700'
                }}>
                  🎨 AI 이미지
                </div>
              )}
            </div>
            <h1 style={{ 
              fontSize: '32px',
              fontWeight: '800',
              color: '#2c3e50',
              lineHeight: '1.2',
              margin: '0',
              marginBottom: '8px'
            }}>
              {board.title}
            </h1>
            {/* 작성자 닉네임 표시 */}
            <div style={{
              fontSize: '14px',
              color: '#6c757d',
              fontWeight: '500',
              marginBottom: '16px'
            }}>
              작성자: {board.authorNickname || '알 수 없는 사용자'}
            </div>
          </div>

          {/* 게시글 본문 */}
          <div style={{ 
            fontSize: '18px',
            lineHeight: '1.8', 
            color: '#495057',
            marginBottom: '32px', 
            whiteSpace: 'pre-wrap'
          }}>
            {board.content}
          </div>
          
          {/* AI 생성 이미지 표시 */}
          {board.imageUrl && (
            <div style={{ 
              marginBottom: '32px', 
              textAlign: 'center',
              background: 'linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%)',
              borderRadius: '20px',
              padding: '32px',
              border: '2px solid rgba(102, 126, 234, 0.1)'
            }}>
              <h3 style={{ 
                marginBottom: '20px', 
                color: '#2c3e50',
                fontSize: '24px',
                fontWeight: '700',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '12px'
              }}>
                🎨 AI가 생성한 이미지
              </h3>
              <img 
                src={board.imageUrl} 
                alt="AI Generated Content" 
                style={{ 
                  maxWidth: '100%', 
                  height: 'auto', 
                  borderRadius: '16px',
                  border: '2px solid rgba(102, 126, 234, 0.2)',
                  boxShadow: '0 16px 32px rgba(0,0,0,0.15)'
                }} 
              />
            </div>
          )}
        </div>

        {/* 좋아요 버튼 */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <LikeButton boardId={board.id} />
        </div>

        {/* 감성분석 결과 */}
        <SentimentAnalysis boardId={board.id} />


        {/* 댓글 섹션 */}
        <div style={{
          background: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          borderRadius: '24px',
          padding: '32px',
          boxShadow: '0 16px 32px rgba(0,0,0,0.1), 0 4px 8px rgba(0,0,0,0.05)',
          border: '1px solid rgba(255,255,255,0.2)'
        }}>
          <CommentSection boardId={board.id} />
        </div>
      </div>
    </div>
  );
}

export default BoardDetail;
