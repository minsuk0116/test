// React 라이브러리와 useState 훅 임포트
import React, { useState } from 'react';
// HTTP 요청을 위한 axios 라이브러리
import axios from 'axios';
// 페이지 이동을 위한 React Router 훅
import { useNavigate } from 'react-router-dom';
// AI 이미지 생성 모달 컴포넌트
import AiImageModal from './AiImageModal';
// 인증 정보를 위한 AuthContext
import { useAuth } from '../contexts/AuthContext';

/**
 * BoardCreate 컴포넌트
 * 새로운 게시글을 작성하는 페이지
 * 
 * 주요 기능:
 * - 게시글 제목, 내용, 타입 입력
 * - AI 이미지 생성 및 첨부
 * - 폼 검증 및 서버 전송
 * - 작성 완료 후 목록 페이지로 이동
 */
function BoardCreate() {
  // 페이지 이동을 위한 React Router 훅
  const navigate = useNavigate();
  // 현재 로그인한 사용자 정보
  const { user } = useAuth();

  // === 상태 변수들 정의 ===
  // 게시글 제목을 저장하는 상태
  const [title, setTitle] = useState('');
  
  // 게시글 내용을 저장하는 상태
  const [content, setContent] = useState('');
  
  // 게시판 타입을 저장하는 상태 (기본값: 사용자 권한에 따라 결정)
  const [boardType, setBoardType] = useState(() => {
    // 사용자 권한에 따른 기본 게시판 타입 설정
    if (!user) return 'FREE';
    if (user.role === 'ADMIN') return 'NOTICE';
    if (user.role === 'COMPANY') return 'COMPANY'; 
    return 'FREE';
  });
  
  // AI가 생성한 이미지 URL을 저장하는 상태
  const [generatedImageUrl, setGeneratedImageUrl] = useState('');
  
  // AI 이미지 생성 모달의 열림/닫힘 상태
  const [isModalOpen, setIsModalOpen] = useState(false);

  /**
   * 폼 제출 핸들러 함수
   * 사용자가 입력한 데이터를 서버에 전송하여 새 게시글 생성
   */
  const handleSubmit = (e) => {
    // 폼의 기본 제출 동작(페이지 새로고침) 방지
    e.preventDefault();

    // 로그인 확인
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    // 서버에 전송할 데이터 객체 생성
    const data = {
      title,                    // 게시글 제목
      content,                  // 게시글 내용
      boardType,               // 게시판 타입
      imageUrl: generatedImageUrl, // AI 생성 이미지 URL (있는 경우)
      authorId: user.id        // 작성자 ID 추가
    };

    // POST 요청으로 새 게시글 생성
    axios.post('http://localhost:8080/boards', data)
      .then(Response => {
        // 성공시 사용자에게 알림 후 목록 페이지로 이동
        alert('게시글이 작성되었습니다');
        navigate('/');
      })
      .catch(error => {
        // 실패시 에러 로그 출력 및 사용자에게 알림
        console.error('게시글 작성 실패:', error);
        
        // 권한 관련 에러 메시지 처리
        if (error.response && error.response.status === 500) {
          const errorMessage = error.response.data?.message || error.message;
          if (errorMessage.includes('공지사항은 관리자만')) {
            alert('권한이 없습니다. 공지사항은 관리자만 작성할 수 있습니다.');
          } else if (errorMessage.includes('기업게시판은 회사원 이상의 권한이')) {
            alert('권한이 없습니다. 기업게시판은 회사원 이상의 권한이 필요합니다.');
          } else {
            alert('오류가 발생했습니다: ' + errorMessage);
          }
        } else {
          alert('오류가 발생했습니다.');
        }
      });
  };

  /**
   * AI 이미지 생성 완료 시 호출되는 콜백 함수
   * AiImageModal 컴포넌트에서 이미지 생성 완료 후 호출됨
   */
  const handleImageGenerated = (imageUrl) => {
    setGeneratedImageUrl(imageUrl);
  };

  /**
   * 사용자 권한에 따라 접근 가능한 게시판 타입을 반환
   */
  const getAvailableBoardTypes = () => {
    if (!user) return ['FREE', 'QNA'];
    
    const allTypes = ['FREE', 'QNA']; // 모든 사용자가 접근 가능한 기본 게시판
    
    if (user.role === 'ADMIN') {
      return ['NOTICE', 'COMPANY', 'FREE', 'QNA']; // 관리자는 모든 게시판 접근 가능
    } else if (user.role === 'COMPANY') {
      return ['COMPANY', 'FREE', 'QNA']; // 회사원은 기업게시판 + 기본 게시판
    }
    
    return allTypes; // 일반회원은 기본 게시판만
  };

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
        {/* 헤더 */}
        <div style={{
          background: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          borderRadius: '24px',
          padding: '32px',
          marginBottom: '32px',
          boxShadow: '0 20px 40px rgba(0,0,0,0.1), 0 8px 16px rgba(0,0,0,0.1)',
          border: '1px solid rgba(255,255,255,0.2)',
          textAlign: 'center'
        }}>
          <div style={{
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
            fontSize: '36px',
            fontWeight: '800',
            margin: '0',
            letterSpacing: '-0.02em'
          }}>
            ✨ 새로운 이야기 작성
          </div>
          <p style={{
            fontSize: '16px',
            color: '#6c757d',
            margin: '12px 0 0 0',
            fontWeight: '400'
          }}>
            AI와 함께 창작하는 특별한 경험
          </p>
        </div>

        {/* 메인 폼 */}
        <div style={{
          background: 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(20px)',
          borderRadius: '24px',
          padding: '40px',
          boxShadow: '0 20px 40px rgba(0,0,0,0.1), 0 8px 16px rgba(0,0,0,0.1)',
          border: '1px solid rgba(255,255,255,0.2)'
        }}>
          <form onSubmit={handleSubmit}>
            {/* 제목 입력 */}
            <div style={{ marginBottom: '28px' }}>
              <label style={{ 
                display: 'block', 
                marginBottom: '12px',
                fontSize: '18px',
                fontWeight: '700',
                color: '#2c3e50'
              }}>
                📝 제목
              </label>
              <input
                type="text"
                value={title}
                onChange={e => setTitle(e.target.value)}
                required
                placeholder="흥미로운 제목을 입력해주세요..."
                style={{ 
                  width: '100%', 
                  padding: '20px 24px', 
                  border: '2px solid rgba(102, 126, 234, 0.1)',
                  borderRadius: '16px',
                  fontSize: '16px',
                  fontWeight: '600',
                  background: 'rgba(255, 255, 255, 0.8)',
                  transition: 'all 0.3s ease',
                  outline: 'none',
                  fontFamily: 'inherit'
                }}
                onFocus={(e) => {
                  e.target.style.borderColor = '#667eea';
                  e.target.style.background = 'rgba(255, 255, 255, 1)';
                  e.target.style.boxShadow = '0 8px 24px rgba(102, 126, 234, 0.15)';
                }}
                onBlur={(e) => {
                  e.target.style.borderColor = 'rgba(102, 126, 234, 0.1)';
                  e.target.style.background = 'rgba(255, 255, 255, 0.8)';
                  e.target.style.boxShadow = 'none';
                }}
              />
            </div>

            {/* 내용 입력 */}
            <div style={{ marginBottom: '28px' }}>
              <label style={{ 
                display: 'block', 
                marginBottom: '12px',
                fontSize: '18px',
                fontWeight: '700',
                color: '#2c3e50'
              }}>
                ✍️ 내용
              </label>
              <textarea
                value={content}
                onChange={e => setContent(e.target.value)}
                required
                rows="12"
                placeholder="여러분의 이야기를 들려주세요..."
                style={{ 
                  width: '100%', 
                  padding: '24px', 
                  border: '2px solid rgba(102, 126, 234, 0.1)',
                  borderRadius: '16px',
                  fontSize: '16px',
                  lineHeight: '1.6',
                  background: 'rgba(255, 255, 255, 0.8)',
                  transition: 'all 0.3s ease',
                  outline: 'none',
                  resize: 'vertical',
                  fontFamily: 'inherit'
                }}
                onFocus={(e) => {
                  e.target.style.borderColor = '#667eea';
                  e.target.style.background = 'rgba(255, 255, 255, 1)';
                  e.target.style.boxShadow = '0 8px 24px rgba(102, 126, 234, 0.15)';
                }}
                onBlur={(e) => {
                  e.target.style.borderColor = 'rgba(102, 126, 234, 0.1)';
                  e.target.style.background = 'rgba(255, 255, 255, 0.8)';
                  e.target.style.boxShadow = 'none';
                }}
              />
            </div>

            {/* 게시판 종류 선택 */}
            <div style={{ marginBottom: '32px' }}>
              <label style={{ 
                display: 'block', 
                marginBottom: '12px',
                fontSize: '18px',
                fontWeight: '700',
                color: '#2c3e50'
              }}>
                📂 게시판 종류
              </label>
              <div style={{
                display: 'flex',
                gap: '12px',
                flexWrap: 'wrap'
              }}>
                {getAvailableBoardTypes().map((type) => {
                  const labels = { 
                    NOTICE: '📢 공지사항', 
                    COMPANY: '🏢 기업게시판', 
                    FREE: '💬 자유게시판', 
                    QNA: '❓ Q&A' 
                  };
                  return (
                    <button
                      key={type}
                      type="button"
                      onClick={() => setBoardType(type)}
                      style={{
                        padding: '16px 24px',
                        border: 'none',
                        borderRadius: '16px',
                        background: boardType === type 
                          ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' 
                          : 'rgba(102, 126, 234, 0.1)',
                        color: boardType === type ? 'white' : '#667eea',
                        fontWeight: '700',
                        fontSize: '16px',
                        cursor: 'pointer',
                        transition: 'all 0.3s ease',
                        boxShadow: boardType === type 
                          ? '0 8px 20px rgba(102, 126, 234, 0.4)' 
                          : 'none',
                        transform: boardType === type ? 'translateY(-2px)' : 'translateY(0)'
                      }}
                      onMouseEnter={(e) => {
                        if (boardType !== type) {
                          e.target.style.background = 'rgba(102, 126, 234, 0.2)';
                          e.target.style.transform = 'translateY(-1px)';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (boardType !== type) {
                          e.target.style.background = 'rgba(102, 126, 234, 0.1)';
                          e.target.style.transform = 'translateY(0)';
                        }
                      }}
                    >
                      {labels[type]}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* 기업게시판 안내 섹션 */}
            {boardType === 'COMPANY' && (
              <div style={{ 
                marginBottom: '32px', 
                padding: '32px',
                background: 'linear-gradient(135deg, rgba(255, 165, 0, 0.05) 0%, rgba(255, 140, 0, 0.05) 100%)',
                borderRadius: '20px', 
                border: '2px solid rgba(255, 165, 0, 0.1)'
              }}>
                <div style={{ 
                  display: 'flex',
                  alignItems: 'center',
                  marginBottom: '20px'
                }}>
                  <div style={{ fontSize: '32px', marginRight: '16px' }}>🏢</div>
                  <div>
                    <h4 style={{ 
                      margin: '0 0 4px 0', 
                      color: '#2c3e50',
                      fontSize: '20px',
                      fontWeight: '700'
                    }}>
                      기업게시판 작성 가이드
                    </h4>
                    <p style={{ 
                      fontSize: '14px', 
                      color: '#6c757d', 
                      margin: '0'
                    }}>
                      기업 관련 정보, 채용공고, 투자 소식, 컨퍼런스 안내 등을 공유해주세요.
                    </p>
                  </div>
                </div>
                
                <div style={{
                  background: 'rgba(255, 255, 255, 0.8)',
                  borderRadius: '16px',
                  padding: '20px',
                  border: '2px solid rgba(255, 165, 0, 0.1)'
                }}>
                  <h5 style={{ margin: '0 0 12px 0', color: '#2c3e50', fontSize: '16px', fontWeight: '600' }}>
                    📝 작성 예시:
                  </h5>
                  <ul style={{ 
                    margin: '0', 
                    paddingLeft: '20px', 
                    color: '#6c757d', 
                    fontSize: '14px',
                    lineHeight: '1.6'
                  }}>
                    <li>💼 채용공고: "삼성전자 2025 신입사원 모집"</li>
                    <li>📊 투자정보: "핀테크 스타트업 투자 설명회"</li>
                    <li>🎯 기업소식: "네이버 신규 서비스 출시 안내"</li>
                    <li>🎤 컨퍼런스: "IT 개발자 컨퍼런스 2025"</li>
                  </ul>
                </div>
              </div>
            )}

            {/* AI 이미지 생성 섹션 - 자유게시판에서만 표시 */}
            {boardType === 'FREE' && (
              <div style={{ 
                marginBottom: '32px', 
                padding: '32px',
                background: 'linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%)',
                borderRadius: '20px', 
                border: '2px solid rgba(102, 126, 234, 0.1)'
              }}>
                <div style={{ 
                  display: 'flex',
                  alignItems: 'center',
                  marginBottom: '20px'
                }}>
                  <div style={{ fontSize: '32px', marginRight: '16px' }}>🎨</div>
                  <div>
                    <h4 style={{ 
                      margin: '0 0 4px 0', 
                      color: '#2c3e50',
                      fontSize: '20px',
                      fontWeight: '700'
                    }}>
                      AI 이미지 생성
                    </h4>
                    <p style={{ 
                      fontSize: '14px', 
                      color: '#6c757d', 
                      margin: '0'
                    }}>
                      원하는 이미지 내용과 스타일을 선택하여 AI 이미지를 생성합니다.
                    </p>
                  </div>
                </div>
                
                <button 
                  type="button"
                  onClick={() => setIsModalOpen(true)}
                  style={{ 
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white', 
                    padding: '16px 32px',
                    border: 'none',
                    borderRadius: '16px',
                    cursor: 'pointer',
                    fontSize: '16px',
                    fontWeight: '700',
                    boxShadow: '0 8px 20px rgba(102, 126, 234, 0.3)',
                    transition: 'all 0.3s ease',
                    marginBottom: '20px'
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.transform = 'translateY(-2px)';
                    e.target.style.boxShadow = '0 12px 30px rgba(102, 126, 234, 0.4)';
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.transform = 'translateY(0)';
                    e.target.style.boxShadow = '0 8px 20px rgba(102, 126, 234, 0.3)';
                  }}
                >
                  ✨ AI 이미지 생성하기
                </button>

                {generatedImageUrl && (
                  <div style={{ 
                    background: 'rgba(255, 255, 255, 0.8)',
                    borderRadius: '16px',
                    padding: '20px',
                    border: '2px solid rgba(102, 126, 234, 0.1)'
                  }}>
                    <p style={{ 
                      fontSize: '16px', 
                      color: '#28a745', 
                      marginBottom: '12px',
                      fontWeight: '700',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}>
                      ✅ 생성된 AI 이미지
                    </p>
                    <img 
                      src={generatedImageUrl} 
                      alt="AI Generated" 
                      style={{ 
                        width: '100%', 
                        height: 'auto', 
                        borderRadius: '12px',
                        border: '2px solid rgba(102, 126, 234, 0.1)',
                        boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
                        marginBottom: '16px'
                      }} 
                    />
                    <button
                      type="button"
                      onClick={() => setGeneratedImageUrl('')}
                      style={{
                        backgroundColor: '#dc3545',
                        color: 'white',
                        border: 'none',
                        borderRadius: '12px',
                        padding: '12px 20px',
                        fontSize: '14px',
                        fontWeight: '600',
                        cursor: 'pointer',
                        transition: 'all 0.3s ease'
                      }}
                      onMouseEnter={(e) => {
                        e.target.style.backgroundColor = '#c82333';
                        e.target.style.transform = 'translateY(-1px)';
                      }}
                      onMouseLeave={(e) => {
                        e.target.style.backgroundColor = '#dc3545';
                        e.target.style.transform = 'translateY(0)';
                      }}
                    >
                      🗑️ 이미지 제거
                    </button>
                  </div>
                )}
              </div>
            )}

            {/* 버튼 그룹 */}
            <div style={{ 
              display: 'flex', 
              gap: '16px',
              justifyContent: 'center',
              marginTop: '40px'
            }}>
              <button 
                type="submit"
                style={{ 
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: 'white', 
                  padding: '20px 40px',
                  border: 'none',
                  borderRadius: '50px',
                  fontSize: '18px',
                  fontWeight: '700',
                  cursor: 'pointer',
                  boxShadow: '0 12px 30px rgba(102, 126, 234, 0.4)',
                  transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
                  position: 'relative',
                  overflow: 'hidden'
                }}
                onMouseEnter={(e) => {
                  e.target.style.transform = 'translateY(-3px) scale(1.02)';
                  e.target.style.boxShadow = '0 20px 40px rgba(102, 126, 234, 0.5)';
                }}
                onMouseLeave={(e) => {
                  e.target.style.transform = 'translateY(0) scale(1)';
                  e.target.style.boxShadow = '0 12px 30px rgba(102, 126, 234, 0.4)';
                }}
              >
                ✨ 작성 완료
              </button>
              <button 
                type="button"
                onClick={() => navigate('/')}
                style={{ 
                  background: 'rgba(108, 117, 125, 0.1)',
                  color: '#6c757d', 
                  padding: '20px 40px',
                  border: '2px solid rgba(108, 117, 125, 0.2)',
                  borderRadius: '50px',
                  fontSize: '18px',
                  fontWeight: '700',
                  cursor: 'pointer',
                  transition: 'all 0.3s ease'
                }}
                onMouseEnter={(e) => {
                  e.target.style.background = 'rgba(108, 117, 125, 0.2)';
                  e.target.style.transform = 'translateY(-1px)';
                  e.target.style.color = '#495057';
                }}
                onMouseLeave={(e) => {
                  e.target.style.background = 'rgba(108, 117, 125, 0.1)';
                  e.target.style.transform = 'translateY(0)';
                  e.target.style.color = '#6c757d';
                }}
              >
                취소
              </button>
            </div>
          </form>
        </div>

        {/* AI 이미지 생성 모달 */}
        <AiImageModal 
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          onImageGenerated={handleImageGenerated}
        />
      </div>
    </div>
  );
}

export default BoardCreate;