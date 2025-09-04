// React 라이브러리 임포트
import React from 'react';
// React Router의 Link (페이지 이동)와 useLocation (현재 경로 확인) 훅 임포트
import { Link, useLocation, useNavigate } from 'react-router-dom';
// 인증 상태 관리를 위한 커스텀 훅
import { useAuth } from '../contexts/AuthContext';

/**
 * Navigation 컴포넌트
 * 웹사이트 상단에 고정되는 네비게이션 바
 * 로고와 메뉴 링크들을 포함
 */
const Navigation = () => {
  // 현재 페이지의 위치 정보를 가져오는 훅 (활성 메뉴 표시용)
  const location = useLocation();
  // 프로그래밍적 페이지 이동을 위한 훅
  const navigate = useNavigate();
  // 인증 상태 및 권한 관리
  const { user, logout, canAccessCompanyBoard } = useAuth();

  /**
   * 로그아웃 핸들러
   */
  const handleLogout = () => {
    if (window.confirm('로그아웃 하시겠습니까?')) {
      logout();
      alert('로그아웃되었습니다.');
      navigate('/');
    }
  };

  /**
   * 네비게이션 메뉴 항목들을 정의하는 배열
   * path: 라우터 경로, label: 표시될 텍스트, icon: 이모지 아이콘
   */
  const navItems = [
    { path: '/', label: '커뮤니티', icon: '💬' },
    { path: '/news', label: '기업 뉴스', icon: '📰' }
  ];

  // === JSX 렌더링 시작 ===
  return (
    // HTML의 nav 태그 - 네비게이션을 위한 시맨틱 태그
    <nav style={{
      backgroundColor: '#2c3e50',               // 어두운 청회색 배경
      padding: '0',                            // 내부 여백 제거
      boxShadow: '0 2px 10px rgba(0,0,0,0.1)', // 아래쪽 그림자 효과
      position: 'sticky',                      // 스크롤시 상단에 고정
      top: '0',                               // 상단에서 0px 위치에 고정
      zIndex: '1000'                          // 다른 요소들 위에 표시 (높은 z-index)
    }}>
      {/* 내부 컨텐츠를 감싸는 컨테이너 - 최대 너비 제한 및 플렉스 레이아웃 */}
      <div style={{
        maxWidth: '1200px',  // 최대 너비 제한
        margin: '0 auto',    // 중앙 정렬
        display: 'flex',     // 플렉스박스 레이아웃
        alignItems: 'center' // 수직 중앙 정렬
      }}>
        
        {/* 로고 링크 - 클릭하면 홈페이지로 이동 */}
        <Link 
          to="/"  // 루트 경로(홈페이지)로 이동
          style={{
            textDecoration: 'none',              // 링크 밑줄 제거
            color: 'white',                      // 흰색 텍스트
            fontSize: '24px',                    // 큰 폰트 크기
            fontWeight: 'bold',                  // 굵은 글씨
            padding: '16px 20px',               // 위아래 16px, 좌우 20px 패딩
            borderRight: '1px solid #34495e'    // 오른쪽에 구분선
          }}
        >
          🏢 JobAtDa
        </Link>
        
        {/* 메뉴 항목들을 감싸는 컨테이너 */}
        <div style={{ display: 'flex' }}>
          {/* navItems 배열을 순회하며 각 메뉴 항목을 렌더링 */}
          {navItems.map((item) => (
            <Link
              key={item.path}  // React에서 리스트 렌더링시 필요한 고유 키
              to={item.path}   // 링크 대상 경로
              style={{
                textDecoration: 'none',          // 링크 밑줄 제거
                color: 'white',                  // 흰색 텍스트
                padding: '16px 24px',           // 클릭 영역 확장을 위한 패딩
                display: 'flex',                // 아이콘과 텍스트를 나란히 배치
                alignItems: 'center',           // 수직 중앙 정렬
                gap: '8px',                     // 아이콘과 텍스트 사이 간격
                fontSize: '16px',               // 폰트 크기
                fontWeight: '500',              // 중간 굵기
                // 현재 페이지와 일치하면 활성 상태 스타일 적용
                backgroundColor: location.pathname === item.path ? '#3498db' : 'transparent',
                borderBottom: location.pathname === item.path ? '3px solid #e74c3c' : '3px solid transparent',
                transition: 'all 0.2s ease'    // 모든 스타일 변화에 부드러운 애니메이션
              }}
              // 마우스 호버 시 배경색 변경 (현재 페이지가 아닌 경우만)
              onMouseEnter={(e) => {
                if (location.pathname !== item.path) {
                  e.target.style.backgroundColor = '#34495e';
                }
              }}
              // 마우스가 벗어날 때 원래 상태로 복원
              onMouseLeave={(e) => {
                if (location.pathname !== item.path) {
                  e.target.style.backgroundColor = 'transparent';
                }
              }}
            >
              {/* 아이콘 표시 */}
              <span>{item.icon}</span>
              {/* 텍스트 라벨 표시 */}
              <span>{item.label}</span>
            </Link>
          ))}
        </div>
        
        {/* 로그인/로그아웃 버튼 영역 */}
        <div style={{ display: 'flex', alignItems: 'center', marginLeft: 'auto', paddingRight: '20px' }}>
          {user ? (
            // 로그인한 경우: 사용자 정보와 로그아웃 버튼 표시
            <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
              {/* 사용자 정보 표시 */}
              <div style={{
                color: 'white',
                fontSize: '14px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <span style={{ fontSize: '16px' }}>👤</span>
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                  <span style={{ fontWeight: '600' }}>{user.nickname}</span>
                  <span style={{ 
                    fontSize: '12px', 
                    opacity: 0.8,
                    color: user.role === 'ADMIN' ? '#e74c3c' : user.role === 'COMPANY' ? '#f39c12' : '#95a5a6'
                  }}>
                    {user.role === 'ADMIN' ? '관리자' : user.role === 'COMPANY' ? '회사원' : '일반회원'}
                  </span>
                </div>
              </div>
              
              {/* 마이페이지 버튼 */}
              <Link
                to="/mypage"
                style={{
                  backgroundColor: '#9b59b6',
                  color: 'white',
                  textDecoration: 'none',
                  padding: '8px 16px',
                  borderRadius: '6px',
                  fontSize: '14px',
                  fontWeight: '500',
                  cursor: 'pointer',
                  transition: 'background-color 0.2s ease'
                }}
                onMouseEnter={(e) => e.target.style.backgroundColor = '#8e44ad'}
                onMouseLeave={(e) => e.target.style.backgroundColor = '#9b59b6'}
              >
                마이페이지
              </Link>
              
              {/* 로그아웃 버튼 */}
              <button
                onClick={handleLogout}
                style={{
                  backgroundColor: '#e74c3c',
                  color: 'white',
                  border: 'none',
                  padding: '8px 16px',
                  borderRadius: '6px',
                  fontSize: '14px',
                  fontWeight: '500',
                  cursor: 'pointer',
                  transition: 'background-color 0.2s ease'
                }}
                onMouseEnter={(e) => e.target.style.backgroundColor = '#c0392b'}
                onMouseLeave={(e) => e.target.style.backgroundColor = '#e74c3c'}
              >
                로그아웃
              </button>
            </div>
          ) : (
            // 로그인하지 않은 경우: 로그인/회원가입 버튼 표시
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Link
                to="/login"
                style={{
                  backgroundColor: '#3498db',
                  color: 'white',
                  textDecoration: 'none',
                  padding: '8px 16px',
                  borderRadius: '6px',
                  fontSize: '14px',
                  fontWeight: '500',
                  transition: 'background-color 0.2s ease'
                }}
                onMouseEnter={(e) => e.target.style.backgroundColor = '#2980b9'}
                onMouseLeave={(e) => e.target.style.backgroundColor = '#3498db'}
              >
                로그인
              </Link>
              
              <Link
                to="/signup"
                style={{
                  backgroundColor: '#2ecc71',
                  color: 'white',
                  textDecoration: 'none',
                  padding: '8px 16px',
                  borderRadius: '6px',
                  fontSize: '14px',
                  fontWeight: '500',
                  transition: 'background-color 0.2s ease'
                }}
                onMouseEnter={(e) => e.target.style.backgroundColor = '#27ae60'}
                onMouseLeave={(e) => e.target.style.backgroundColor = '#2ecc71'}
              >
                회원가입
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

// 컴포넌트를 다른 파일에서 import할 수 있도록 기본 내보내기
export default Navigation;