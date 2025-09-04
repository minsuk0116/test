// React 훅들 임포트
import React, { useEffect, useState } from 'react';
// HTTP 요청 라이브러리
import axios from 'axios';

/**
 * SentimentAnalysis 컴포넌트
 * AI를 활용한 게시글 감성분석 결과를 표시하는 컴포넌트
 * 
 * Props:
 * - boardId: 분석할 게시글의 ID
 * 
 * 기능:
 * - 게시글 내용의 감정 분석 (긍정/부정/중립)
 * - 분석 결과 시각화
 * - 상세 분석 결과 토글 표시
 */
const SentimentAnalysis = ({ boardId }) => {
  // 감성분석 결과 데이터
  const [sentiment, setSentiment] = useState(null);
  // 로딩 상태
  const [loading, setLoading] = useState(true);
  // 에러 상태
  const [error, setError] = useState(null);
  // 상세 정보 표시 여부
  const [showDetails, setShowDetails] = useState(false);

  // boardId가 변경될 때마다 감성분석 실행
  useEffect(() => {
    fetchSentiment();
  }, [boardId]);

  /**
   * 감성분석 API 호출 함수
   */
  const fetchSentiment = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // AI 감성분석 API 호출
      const response = await axios.get(`http://localhost:8080/api/sentiment/analyze/${boardId}`);
      
      if (response.data.success) {
        setSentiment(response.data);
      } else {
        setError(response.data.error || '감성분석에 실패했습니다.');
      }
    } catch (err) {
      console.error('감성분석 요청 실패:', err);
      setError('감성분석 서비스에 연결할 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const getSentimentText = (sentimentType) => {
    switch (sentimentType) {
      case 'POSITIVE': return '긍정적';
      case 'NEGATIVE': return '부정적';
      default: return '중립적';
    }
  };

  const getConfidenceColor = (confidence) => {
    if (confidence >= 80) return '#28a745';
    if (confidence >= 60) return '#ffc107';
    return '#dc3545';
  };

  if (loading) {
    return (
      <div style={{
        margin: '20px 0',
        padding: '15px',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        border: '1px solid #e9ecef',
        textAlign: 'center'
      }}>
        <div style={{ color: '#6c757d', fontSize: '14px' }}>
          🤖 AI가 게시글의 감성을 분석하고 있습니다...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{
        margin: '20px 0',
        padding: '15px',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        border: '1px solid #e9ecef',
        textAlign: 'center'
      }}>
        <div style={{ color: '#dc3545', fontSize: '14px' }}>
          ⚠️ 감성분석을 수행할 수 없습니다: {error}
        </div>
        <button
          onClick={fetchSentiment}
          style={{
            marginTop: '8px',
            padding: '4px 12px',
            backgroundColor: '#6c757d',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            fontSize: '12px',
            cursor: 'pointer'
          }}
        >
          다시 시도
        </button>
      </div>
    );
  }

  if (!sentiment) return null;

  return (
    <div style={{
      margin: '20px 0',
      padding: '15px',
      backgroundColor: '#fff',
      borderRadius: '8px',
      border: `2px solid ${sentiment.color}`,
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
    }}>
      {/* 메인 감성분석 결과 */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: '10px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span style={{ fontSize: '24px' }}>{sentiment.icon}</span>
          <div>
            <div style={{
              fontSize: '16px',
              fontWeight: '600',
              color: sentiment.color
            }}>
              🤖 AI 감성분석: {getSentimentText(sentiment.sentiment)}
            </div>
            <div style={{ fontSize: '14px', color: '#6c757d', marginTop: '2px' }}>
              {sentiment.summary}
            </div>
          </div>
        </div>
        
        <div style={{ textAlign: 'right' }}>
          <div style={{
            fontSize: '12px',
            color: getConfidenceColor(sentiment.confidence),
            fontWeight: '600'
          }}>
            신뢰도: {sentiment.confidence}%
          </div>
          <button
            onClick={() => setShowDetails(!showDetails)}
            style={{
              marginTop: '4px',
              padding: '2px 8px',
              backgroundColor: 'transparent',
              color: sentiment.color,
              border: `1px solid ${sentiment.color}`,
              borderRadius: '4px',
              fontSize: '12px',
              cursor: 'pointer'
            }}
          >
            {showDetails ? '간단히 보기' : '자세히 보기'}
          </button>
        </div>
      </div>

      {/* 상세 정보 */}
      {showDetails && (
        <div style={{
          marginTop: '15px',
          padding: '12px',
          backgroundColor: '#f8f9fa',
          borderRadius: '6px',
          border: '1px solid #e9ecef'
        }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <div style={{ fontSize: '12px', color: '#6c757d', marginBottom: '4px' }}>
                감정 상태
              </div>
              <div style={{ fontSize: '14px', fontWeight: '500', color: '#495057' }}>
                {sentiment.emotion}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '12px', color: '#6c757d', marginBottom: '4px' }}>
                톤 & 분위기
              </div>
              <div style={{ fontSize: '14px', fontWeight: '500', color: '#495057' }}>
                {sentiment.tone}
              </div>
            </div>
          </div>
          
          <div style={{ marginTop: '12px', fontSize: '12px', color: '#6c757d' }}>
            💡 이 분석은 AI(GPT)가 게시글의 텍스트를 분석한 결과입니다.
          </div>
        </div>
      )}
    </div>
  );
};

export default SentimentAnalysis;