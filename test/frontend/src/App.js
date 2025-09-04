// React 라이브러리 임포트 - 컴포넌트 작성을 위한 핵심 라이브러리
import React from 'react';
// 인증 관리 Context
import { AuthProvider } from './contexts/AuthContext';

// React Router 라이브러리에서 라우팅 관련 컴포넌트들 임포트
// BrowserRouter: HTML5 History API를 사용하는 라우터 (as Router로 별칭 지정)
// Routes: 여러 Route를 감싸는 컨테이너
// Route: 특정 경로와 컴포넌트를 매핑하는 역할
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

// 각 페이지별 컴포넌트들 임포트
import BoardList from './components/BoardList';         // 게시글 목록 페이지
import BoardDetail from './components/BoardDetail'      // 게시글 상세보기 페이지  
import BoardCreate from './components/BoardCreate';     // 게시글 작성 페이지
import BoardEdit from './components/BoardEdit';         // 게시글 수정 페이지
import CompanyNews from './components/CompanyNews';     // 기업뉴스 페이지
import Navigation from './components/Navigation';       // 상단 네비게이션 바
import Login from './components/Login';                 // 로그인 페이지
import SignUp from './components/SignUp';               // 회원가입 페이지
import MyPage from './components/MyPage';               // 마이페이지

// CSS 스타일 파일 임포트
import './App.css';

/**
 * App 컴포넌트 - 애플리케이션의 최상위(루트) 컴포넌트
 * 
 * 주요 역할:
 * 1. 라우터 설정: URL 경로에 따라 어떤 컴포넌트를 보여줄지 결정
 * 2. 공통 레이아웃: 모든 페이지에서 공통으로 사용되는 네비게이션 바 포함
 * 3. SPA(Single Page Application) 구현: 페이지 새로고침 없이 컴포넌트만 교체
 */
function App() {
  return (
    // AuthProvider로 인증 상태를 전역 관리
    <AuthProvider>
      {/* Router로 전체 애플리케이션을 감싸서 라우팅 기능 활성화 */}
      <Router>
      {/* 모든 페이지에서 공통으로 표시되는 상단 네비게이션 */}
      <Navigation />
      
      {/* Routes 컴포넌트: 여러 Route들을 관리하는 컨테이너 */}
      <Routes>
        {/* 각 Route: path(URL 경로)와 element(렌더링할 컴포넌트)를 매핑 */}
        
        {/* 홈페이지 - 게시글 목록을 보여줌 */}
        <Route path="/" element={<BoardList />} />
        
        {/* 게시글 상세보기 - :id는 동적 파라미터 (예: /boards/123) */}
        <Route path="/boards/:id" element={<BoardDetail />} />
        
        {/* 게시글 작성 페이지 */}
        <Route path="/boards/create" element={<BoardCreate />} />
        
        {/* 게시글 수정 페이지 - :id는 수정할 게시글의 ID */}
        <Route path="/boards/edit/:id" element={<BoardEdit />} />
        
        {/* 기업뉴스 페이지 */}
        <Route path="/news" element={<CompanyNews />} />
        
        {/* 로그인 페이지 */}
        <Route path="/login" element={<Login />} />
        
        {/* 회원가입 페이지 */}
        <Route path="/signup" element={<SignUp />} />
        
        {/* 마이페이지 */}
        <Route path="/mypage" element={<MyPage />} />
      </Routes>
      </Router>
    </AuthProvider>
  );
}

// App 컴포넌트를 다른 파일에서 import할 수 있도록 기본 내보내기
// 일반적으로 index.js에서 이 App 컴포넌트를 import해서 DOM에 렌더링함
export default App;
