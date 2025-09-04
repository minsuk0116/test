// React 라이브러리와 필요한 훅들 임포트
import React, { useState, useEffect } from 'react';
// HTTP 요청을 위한 axios 라이브러리
import axios from 'axios';

/**
 * LikeButton 컴포넌트
 * 게시글에 좋아요 기능을 제공하는 인터랙티브 버튼
 * 
 * Props:
 * - boardId: 좋아요를 누를 게시글의 ID
 * - userIdentifier: 사용자 식별자 (기본값: 'anonymous_user')
 * 
 * 주요 기능:
 * - 현재 좋아요 상태 및 개수 표시
 * - 좋아요 토글 기능 (좋아요/좋아요 취소)
 * - 로딩 상태 처리
 * - 실시간 좋아요 수 업데이트
 * - 반응형 애니메이션 효과
 */
function LikeButton({ boardId, userIdentifier = 'anonymous_user' }) {
  // === 상태 변수들 정의 ===
  
  // 현재 사용자가 좋아요를 눌렀는지 여부
  const [liked, setLiked] = useState(false);
  
  // 전체 좋아요 수
  const [likeCount, setLikeCount] = useState(0);
  
  // 버튼 클릭 처리 중인지 여부 (중복 클릭 방지용)
  const [loading, setLoading] = useState(false);

  /**
   * useEffect: 컴포넌트 마운트 시 또는 boardId 변경 시 좋아요 상태 로딩
   * 게시글이 바뀔 때마다 해당 게시글의 좋아요 상태를 새로 불러옴
   */
  useEffect(() => {
    fetchLikeStatus();
  }, [boardId]); // boardId가 변경될 때마다 실행

  /**
   * 좋아요 상태 및 개수를 서버에서 가져오는 비동기 함수
   * 현재 사용자의 좋아요 상태와 전체 좋아요 수를 조회
   */
  const fetchLikeStatus = async () => {
    try {
      // GET 요청으로 좋아요 상태 조회 (쿼리 파라미터로 사용자 식별자 전송)
      const response = await axios.get(`http://localhost:8080/boards/${boardId}/likes`, {
        params: { userIdentifier } // 사용자 식별을 위한 파라미터
      });
      
      // 서버 응답으로 상태 업데이트
      setLiked(response.data.liked);       // 현재 사용자의 좋아요 여부
      setLikeCount(response.data.likeCount); // 전체 좋아요 수
    } catch (error) {
      console.error('좋아요 상태 조회 실패:', error);
    }
  };

  /**
   * 좋아요 토글 핸들러 함수
   * 좋아요 상태를 반대로 바꾸는 기능 (좋아요 ↔ 좋아요 취소)
   */
  const handleLikeToggle = async () => {
    // 이미 처리 중이면 무시 (중복 클릭 방지)
    if (loading) return;

    setLoading(true); // 로딩 상태 시작
    
    try {
      // POST 요청으로 좋아요 토글 (좋아요/취소를 하나의 API로 처리)
      const response = await axios.post(`http://localhost:8080/boards/${boardId}/likes/toggle`, {
        userIdentifier // 사용자 식별자를 요청 바디에 포함
      });
      
      // 서버 응답으로 상태 즉시 업데이트 (UI 반응성 향상)
      setLiked(response.data.liked);       // 토글된 좋아요 상태
      setLikeCount(response.data.likeCount); // 업데이트된 좋아요 수
    } catch (error) {
      console.error('좋아요 토글 실패:', error);
      alert('좋아요 처리에 실패했습니다.');
    } finally {
      setLoading(false); // 성공/실패 관계없이 로딩 상태 종료
    }
  };

  return (
    <button
      onClick={handleLikeToggle}
      disabled={loading}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        padding: '10px 16px',
        border: `2px solid ${liked ? '#e91e63' : '#ddd'}`,
        borderRadius: '25px',
        backgroundColor: liked ? '#fce4ec' : 'white',
        color: liked ? '#e91e63' : '#6c757d',
        cursor: loading ? 'not-allowed' : 'pointer',
        fontSize: '14px',
        fontWeight: '500',
        transition: 'all 0.3s ease',
        opacity: loading ? 0.6 : 1
      }}
      onMouseEnter={(e) => {
        if (!loading) {
          e.target.style.transform = 'translateY(-2px)';
          e.target.style.boxShadow = '0 4px 12px rgba(233, 30, 99, 0.3)';
        }
      }}
      onMouseLeave={(e) => {
        if (!loading) {
          e.target.style.transform = 'translateY(0)';
          e.target.style.boxShadow = 'none';
        }
      }}
    >
      <span style={{ fontSize: '16px' }}>
        {liked ? '❤️' : '🤍'}
      </span>
      <span>
        {loading ? '...' : `좋아요 ${likeCount}`}
      </span>
    </button>
  );
}

export default LikeButton;