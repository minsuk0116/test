// React Context API를 위한 라이브러리들
import React, { createContext, useContext, useState, useEffect } from 'react';

/**
 * AuthContext
 * 애플리케이션 전체에서 사용자 인증 상태를 관리하는 Context
 * 
 * 주요 기능:
 * - 로그인/로그아웃 상태 관리
 * - 사용자 정보 전역 공유  
 * - 권한별 접근 제어 지원
 * - 로컬 스토리지와 동기화
 */

// Context 생성
const AuthContext = createContext();

/**
 * AuthProvider 컴포넌트
 * 인증 관련 상태와 함수들을 하위 컴포넌트들에게 제공
 */
export const AuthProvider = ({ children }) => {
    // 현재 로그인한 사용자 정보
    const [user, setUser] = useState(null);
    // 로딩 상태 (초기 로딩 시 사용)
    const [loading, setLoading] = useState(true);

    /**
     * 컴포넌트 마운트 시 로컬 스토리지에서 사용자 정보 복원
     */
    useEffect(() => {
        try {
            const savedUser = localStorage.getItem('user');
            if (savedUser) {
                const userData = JSON.parse(savedUser);
                setUser(userData);
            }
        } catch (error) {
            console.error('사용자 정보 로드 실패:', error);
            localStorage.removeItem('user'); // 손상된 데이터 제거
        } finally {
            setLoading(false);
        }
    }, []);

    /**
     * 로그인 함수
     * API 로그인 성공 후 호출하여 사용자 정보를 전역 상태로 설정
     * 
     * @param {Object} userData - 로그인한 사용자 정보
     */
    const login = (userData) => {
        try {
            setUser(userData);
            localStorage.setItem('user', JSON.stringify(userData));
        } catch (error) {
            console.error('로그인 정보 저장 실패:', error);
        }
    };

    /**
     * 로그아웃 함수
     * 사용자 정보를 전역 상태와 로컬 스토리지에서 제거
     */
    const logout = () => {
        setUser(null);
        localStorage.removeItem('user');
    };

    /**
     * 로그인 상태 확인
     * @returns {boolean} 로그인 여부
     */
    const isAuthenticated = () => {
        return user !== null;
    };

    /**
     * 관리자 권한 확인
     * @returns {boolean} 관리자 권한 여부
     */
    const isAdmin = () => {
        return user && user.role === 'ADMIN';
    };

    /**
     * 회사원 권한 확인 (관리자 포함)
     * @returns {boolean} 회사원 이상 권한 여부
     */
    const isCompanyUser = () => {
        return user && (user.role === 'ADMIN' || user.role === 'COMPANY');
    };

    /**
     * 일반 사용자 권한 확인
     * @returns {boolean} 일반 사용자 권한 여부
     */
    const isGeneralUser = () => {
        return user && user.role === 'GENERAL';
    };

    /**
     * 공지사항 작성 권한 확인
     * @returns {boolean} 공지사항 작성 가능 여부 (관리자만)
     */
    const canWriteNotice = () => {
        return isAdmin();
    };

    /**
     * 기업게시판 접근 권한 확인
     * @returns {boolean} 기업게시판 접근 가능 여부 (관리자, 회사원만)
     */
    const canAccessCompanyBoard = () => {
        return isCompanyUser();
    };

    /**
     * 게시판별 읽기 권한 확인
     * @param {string} boardType - 게시판 타입 (NOTICE, COMPANY, FREE, QNA)
     * @returns {boolean} 해당 게시판 읽기 권한 여부
     */
    const canReadBoard = (boardType) => {
        if (!isAuthenticated()) return false;
        
        switch (boardType) {
            case 'NOTICE':
            case 'FREE': 
            case 'QNA':
                return true; // 모든 로그인 사용자 접근 가능
            case 'COMPANY':
                return canAccessCompanyBoard(); // 관리자, 회사원만 접근 가능
            default:
                return false;
        }
    };

    /**
     * 게시판별 쓰기 권한 확인
     * @param {string} boardType - 게시판 타입 (NOTICE, COMPANY, FREE, QNA)
     * @returns {boolean} 해당 게시판 쓰기 권한 여부
     */
    const canWriteBoard = (boardType) => {
        if (!isAuthenticated()) return false;
        
        switch (boardType) {
            case 'NOTICE':
                return canWriteNotice(); // 관리자만 작성 가능
            case 'COMPANY':
                return canAccessCompanyBoard(); // 관리자, 회사원만 작성 가능
            case 'FREE':
            case 'QNA':
                return true; // 모든 로그인 사용자 작성 가능
            default:
                return false;
        }
    };

    /**
     * 사용자 정보 업데이트
     * 프로필 수정 등에서 사용
     * @param {Object} updatedUserData - 업데이트된 사용자 정보
     */
    const updateUser = (updatedUserData) => {
        try {
            setUser(updatedUserData);
            localStorage.setItem('user', JSON.stringify(updatedUserData));
        } catch (error) {
            console.error('사용자 정보 업데이트 실패:', error);
        }
    };

    // Context에 제공할 값들
    const value = {
        // 상태
        user,
        loading,
        
        // 인증 관련 함수들
        login,
        logout,
        updateUser,
        
        // 권한 확인 함수들
        isAuthenticated,
        isAdmin,
        isCompanyUser,
        isGeneralUser,
        canWriteNotice,
        canAccessCompanyBoard,
        canReadBoard,
        canWriteBoard
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

/**
 * useAuth Hook
 * AuthContext를 쉽게 사용할 수 있도록 하는 커스텀 훅
 * 
 * @returns {Object} AuthContext의 모든 값들
 */
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};