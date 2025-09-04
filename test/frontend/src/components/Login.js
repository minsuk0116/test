// React 라이브러리와 필요한 훅들 임포트
import React, { useState } from 'react';
// HTTP 요청을 위한 axios 라이브러리
import axios from 'axios';
// React Router 훅들 - 페이지 이동용
import { useNavigate, Link } from 'react-router-dom';
// 인증 상태 관리를 위한 커스텀 훅
import { useAuth } from '../contexts/AuthContext';

/**
 * Login 컴포넌트
 * 사용자 로그인 페이지
 * 
 * 주요 기능:
 * - 사용자명과 비밀번호로 로그인
 * - 로그인 성공 시 사용자 정보를 로컬 스토리지에 저장
 * - 로그인 후 메인 페이지로 이동
 * - 회원가입 페이지로 이동 링크 제공
 */
function Login() {
    // 프로그래밍적 페이지 이동을 위한 훅
    const navigate = useNavigate();
    // 인증 상태 관리
    const { login } = useAuth();

    // === 상태 변수들 정의 ===
    // 로그인 폼 데이터
    const [formData, setFormData] = useState({
        username: '',
        password: ''
    });
    
    // 로딩 상태 (로그인 요청 중일 때 버튼 비활성화)
    const [loading, setLoading] = useState(false);
    
    // 에러 메시지
    const [error, setError] = useState('');

    /**
     * 입력 필드 변경 핸들러
     * 사용자가 입력할 때마다 상태를 업데이트
     */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // 입력 시 에러 메시지 제거
        if (error) setError('');
    };

    /**
     * 로그인 폼 제출 핸들러
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        // 입력 검증
        if (!formData.username.trim() || !formData.password.trim()) {
            setError('ID와 비밀번호를 모두 입력해주세요.');
            return;
        }

        setLoading(true);
        setError('');

        try {
            // 로그인 API 호출
            const response = await axios.post('http://localhost:8080/api/users/login', {
                username: formData.username,
                password: formData.password
            });

            // 로그인 성공
            const { user, message } = response.data;
            
            // AuthContext를 통해 사용자 정보 설정
            login(user);
            
            // 성공 메시지 표시
            alert(message || '로그인 성공!');
            
            // 메인 페이지로 이동
            navigate('/');
            
        } catch (error) {
            // 로그인 실패 처리
            console.error('로그인 오류:', error);
            if (error.response && error.response.data) {
                setError(error.response.data.error || '로그인에 실패했습니다.');
            } else {
                setError('서버 연결에 실패했습니다.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ 
            minHeight: '80vh', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center',
            backgroundColor: '#f8f9fa',
            padding: '20px'
        }}>
            <div style={{
                backgroundColor: 'white',
                padding: '40px',
                borderRadius: '12px',
                boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                width: '100%',
                maxWidth: '400px'
            }}>
                {/* 로그인 헤더 */}
                <div style={{ textAlign: 'center', marginBottom: '30px' }}>
                    <h2 style={{ 
                        color: '#2c3e50', 
                        marginBottom: '10px',
                        fontSize: '28px',
                        fontWeight: 'bold'
                    }}>
                        🔐 로그인
                    </h2>
                    <p style={{ color: '#6c757d', fontSize: '16px' }}>
                        JobAtDa 커뮤니티에 오신 것을 환영합니다
                    </p>
                </div>

                {/* 에러 메시지 */}
                {error && (
                    <div style={{
                        backgroundColor: '#f8d7da',
                        color: '#721c24',
                        padding: '12px',
                        borderRadius: '6px',
                        marginBottom: '20px',
                        border: '1px solid #f5c6cb',
                        fontSize: '14px'
                    }}>
                        {error}
                    </div>
                )}

                {/* 로그인 폼 */}
                <form onSubmit={handleSubmit}>
                    {/* 사용자명 입력 */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ 
                            display: 'block', 
                            marginBottom: '8px',
                            fontWeight: '600',
                            color: '#495057'
                        }}>
                            ID
                        </label>
                        <input
                            type="text"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            placeholder="ID를 입력하세요"
                            required
                            style={{
                                width: '100%',
                                padding: '12px',
                                border: '2px solid #e9ecef',
                                borderRadius: '8px',
                                fontSize: '16px',
                                transition: 'border-color 0.3s',
                                outline: 'none'
                            }}
                            onFocus={(e) => e.target.style.borderColor = '#3498db'}
                            onBlur={(e) => e.target.style.borderColor = '#e9ecef'}
                        />
                    </div>

                    {/* 비밀번호 입력 */}
                    <div style={{ marginBottom: '25px' }}>
                        <label style={{ 
                            display: 'block', 
                            marginBottom: '8px',
                            fontWeight: '600',
                            color: '#495057'
                        }}>
                            비밀번호
                        </label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="비밀번호를 입력하세요"
                            required
                            style={{
                                width: '100%',
                                padding: '12px',
                                border: '2px solid #e9ecef',
                                borderRadius: '8px',
                                fontSize: '16px',
                                transition: 'border-color 0.3s',
                                outline: 'none'
                            }}
                            onFocus={(e) => e.target.style.borderColor = '#3498db'}
                            onBlur={(e) => e.target.style.borderColor = '#e9ecef'}
                        />
                    </div>

                    {/* 로그인 버튼 */}
                    <button
                        type="submit"
                        disabled={loading}
                        style={{
                            width: '100%',
                            padding: '14px',
                            backgroundColor: loading ? '#95a5a6' : '#3498db',
                            color: 'white',
                            border: 'none',
                            borderRadius: '8px',
                            fontSize: '16px',
                            fontWeight: '600',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            transition: 'background-color 0.3s',
                            marginBottom: '20px'
                        }}
                        onMouseEnter={(e) => {
                            if (!loading) e.target.style.backgroundColor = '#2980b9';
                        }}
                        onMouseLeave={(e) => {
                            if (!loading) e.target.style.backgroundColor = '#3498db';
                        }}
                    >
                        {loading ? '로그인 중...' : '로그인'}
                    </button>

                    {/* 구분선 */}
                    <div style={{
                        textAlign: 'center',
                        margin: '20px 0',
                        position: 'relative'
                    }}>
                        <hr style={{ border: 'none', height: '1px', backgroundColor: '#dee2e6' }} />
                        <span style={{
                            position: 'absolute',
                            top: '-10px',
                            left: '50%',
                            transform: 'translateX(-50%)',
                            backgroundColor: 'white',
                            padding: '0 15px',
                            color: '#6c757d',
                            fontSize: '14px'
                        }}>
                            또는
                        </span>
                    </div>

                    {/* 회원가입 링크 */}
                    <div style={{ textAlign: 'center' }}>
                        <span style={{ color: '#6c757d', fontSize: '14px' }}>
                            아직 계정이 없으신가요?{' '}
                        </span>
                        <Link
                            to="/signup"
                            style={{
                                color: '#3498db',
                                textDecoration: 'none',
                                fontWeight: '600',
                                fontSize: '14px'
                            }}
                            onMouseEnter={(e) => e.target.style.textDecoration = 'underline'}
                            onMouseLeave={(e) => e.target.style.textDecoration = 'none'}
                        >
                            회원가입
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Login;