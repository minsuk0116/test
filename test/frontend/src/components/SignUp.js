// React 라이브러리와 필요한 훅들 임포트
import React, { useState } from 'react';
// HTTP 요청을 위한 axios 라이브러리
import axios from 'axios';
// React Router 훅들 - 페이지 이동용
import { useNavigate, Link } from 'react-router-dom';

/**
 * SignUp 컴포넌트
 * 사용자 회원가입 페이지
 * 
 * 주요 기능:
 * - 사용자명, 비밀번호, 닉네임, 이메일 입력
 * - 실시간 중복 체크 (사용자명, 닉네임, 이메일)
 * - 입력 유효성 검증
 * - 회원가입 성공 시 로그인 페이지로 이동
 */
function SignUp() {
    // 프로그래밍적 페이지 이동을 위한 훅
    const navigate = useNavigate();

    // === 상태 변수들 정의 ===
    // 회원가입 폼 데이터
    const [formData, setFormData] = useState({
        username: '',
        password: '',
        confirmPassword: '',
        nickname: '',
        email: '',
        role: 'GENERAL' // 기본값: 일반 사용자
    });
    
    // 로딩 상태
    const [loading, setLoading] = useState(false);
    
    // 에러 메시지
    const [errors, setErrors] = useState({});
    
    // 중복 체크 결과
    const [duplicateCheck, setDuplicateCheck] = useState({
        username: null,
        nickname: null,
        email: null
    });

    /**
     * 입력 필드 변경 핸들러
     */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        
        // 해당 필드의 에러 메시지 제거
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
        
        // 중복 체크 결과 초기화
        if (['username', 'nickname', 'email'].includes(name)) {
            setDuplicateCheck(prev => ({
                ...prev,
                [name]: null
            }));
        }
    };

    /**
     * 사용자명 중복 체크
     */
    const checkDuplicateUsername = async () => {
        if (!formData.username.trim()) {
            setErrors(prev => ({ ...prev, username: '사용자명을 입력해주세요.' }));
            return;
        }

        try {
            const response = await axios.post('http://localhost:8080/api/users/check-username', {
                username: formData.username
            });
            
            const exists = response.data.exists;
            setDuplicateCheck(prev => ({ ...prev, username: !exists }));
            
            if (exists) {
                setErrors(prev => ({ ...prev, username: '이미 사용중인 사용자명입니다.' }));
            } else {
                setErrors(prev => ({ ...prev, username: '' }));
            }
        } catch (error) {
            console.error('사용자명 중복 체크 오류:', error);
            setErrors(prev => ({ ...prev, username: '중복 체크에 실패했습니다.' }));
        }
    };

    /**
     * 닉네임 중복 체크
     */
    const checkDuplicateNickname = async () => {
        if (!formData.nickname.trim()) {
            setErrors(prev => ({ ...prev, nickname: '닉네임을 입력해주세요.' }));
            return;
        }

        try {
            const response = await axios.post('http://localhost:8080/api/users/check-nickname', {
                nickname: formData.nickname
            });
            
            const exists = response.data.exists;
            setDuplicateCheck(prev => ({ ...prev, nickname: !exists }));
            
            if (exists) {
                setErrors(prev => ({ ...prev, nickname: '이미 사용중인 닉네임입니다.' }));
            } else {
                setErrors(prev => ({ ...prev, nickname: '' }));
            }
        } catch (error) {
            console.error('닉네임 중복 체크 오류:', error);
            setErrors(prev => ({ ...prev, nickname: '중복 체크에 실패했습니다.' }));
        }
    };

    /**
     * 이메일 중복 체크
     */
    const checkDuplicateEmail = async () => {
        if (!formData.email.trim()) {
            return; // 이메일은 선택사항
        }

        // 간단한 이메일 형식 검증
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData.email)) {
            setErrors(prev => ({ ...prev, email: '올바른 이메일 형식이 아닙니다.' }));
            return;
        }

        try {
            const response = await axios.post('http://localhost:8080/api/users/check-email', {
                email: formData.email
            });
            
            const exists = response.data.exists;
            setDuplicateCheck(prev => ({ ...prev, email: !exists }));
            
            if (exists) {
                setErrors(prev => ({ ...prev, email: '이미 사용중인 이메일입니다.' }));
            } else {
                setErrors(prev => ({ ...prev, email: '' }));
            }
        } catch (error) {
            console.error('이메일 중복 체크 오류:', error);
            setErrors(prev => ({ ...prev, email: '중복 체크에 실패했습니다.' }));
        }
    };

    /**
     * 폼 유효성 검증
     */
    const validateForm = () => {
        const newErrors = {};

        if (!formData.username.trim()) {
            newErrors.username = '사용자명을 입력해주세요.';
        } else if (formData.username.length < 3) {
            newErrors.username = '사용자명은 3자 이상이어야 합니다.';
        }

        if (!formData.password) {
            newErrors.password = '비밀번호를 입력해주세요.';
        } else if (formData.password.length < 6) {
            newErrors.password = '비밀번호는 6자 이상이어야 합니다.';
        }

        if (!formData.confirmPassword) {
            newErrors.confirmPassword = '비밀번호 확인을 입력해주세요.';
        } else if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = '비밀번호가 일치하지 않습니다.';
        }

        if (!formData.nickname.trim()) {
            newErrors.nickname = '닉네임을 입력해주세요.';
        } else if (formData.nickname.length < 2) {
            newErrors.nickname = '닉네임은 2자 이상이어야 합니다.';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    /**
     * 회원가입 폼 제출 핸들러
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        // 폼 유효성 검증
        if (!validateForm()) {
            return;
        }

        setLoading(true);

        try {
            // 회원가입 API 호출
            const response = await axios.post('http://localhost:8080/api/users/signup', {
                username: formData.username,
                password: formData.password,
                nickname: formData.nickname,
                email: formData.email || null,
                role: formData.role
            });

            // 회원가입 성공
            alert('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
            navigate('/login');
            
        } catch (error) {
            // 회원가입 실패 처리
            console.error('회원가입 오류:', error);
            if (error.response && error.response.data) {
                const errorMessage = error.response.data.error || '회원가입에 실패했습니다.';
                setErrors({ general: errorMessage });
            } else {
                setErrors({ general: '서버 연결에 실패했습니다.' });
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ 
            minHeight: '90vh', 
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
                maxWidth: '500px'
            }}>
                {/* 회원가입 헤더 */}
                <div style={{ textAlign: 'center', marginBottom: '30px' }}>
                    <h2 style={{ 
                        color: '#2c3e50', 
                        marginBottom: '10px',
                        fontSize: '28px',
                        fontWeight: 'bold'
                    }}>
                        📝 회원가입
                    </h2>
                    <p style={{ color: '#6c757d', fontSize: '16px' }}>
                        JobAtDa 커뮤니티에 가입하세요
                    </p>
                </div>

                {/* 일반 에러 메시지 */}
                {errors.general && (
                    <div style={{
                        backgroundColor: '#f8d7da',
                        color: '#721c24',
                        padding: '12px',
                        borderRadius: '6px',
                        marginBottom: '20px',
                        border: '1px solid #f5c6cb',
                        fontSize: '14px'
                    }}>
                        {errors.general}
                    </div>
                )}

                {/* 회원가입 폼 */}
                <form onSubmit={handleSubmit}>
                    {/* 사용자명 입력 */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ 
                            display: 'block', 
                            marginBottom: '8px',
                            fontWeight: '600',
                            color: '#495057'
                        }}>
                            ID *
                        </label>
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <input
                                type="text"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                placeholder="3자 이상의 ID"
                                required
                                style={{
                                    flex: 1,
                                    padding: '12px',
                                    border: `2px solid ${errors.username ? '#dc3545' : duplicateCheck.username === true ? '#28a745' : duplicateCheck.username === false ? '#dc3545' : '#e9ecef'}`,
                                    borderRadius: '8px',
                                    fontSize: '16px',
                                    outline: 'none'
                                }}
                            />
                            <button
                                type="button"
                                onClick={checkDuplicateUsername}
                                style={{
                                    padding: '12px 16px',
                                    backgroundColor: '#6c757d',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '14px',
                                    cursor: 'pointer',
                                    whiteSpace: 'nowrap'
                                }}
                            >
                                중복확인
                            </button>
                        </div>
                        {errors.username && (
                            <div style={{ color: '#dc3545', fontSize: '14px', marginTop: '5px' }}>
                                {errors.username}
                            </div>
                        )}
                        {duplicateCheck.username === true && (
                            <div style={{ color: '#28a745', fontSize: '14px', marginTop: '5px' }}>
                                ✓ 사용 가능한 사용자명입니다.
                            </div>
                        )}
                    </div>

                    {/* 닉네임 입력 */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ 
                            display: 'block', 
                            marginBottom: '8px',
                            fontWeight: '600',
                            color: '#495057'
                        }}>
                            닉네임 *
                        </label>
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <input
                                type="text"
                                name="nickname"
                                value={formData.nickname}
                                onChange={handleChange}
                                placeholder="2자 이상의 닉네임"
                                required
                                style={{
                                    flex: 1,
                                    padding: '12px',
                                    border: `2px solid ${errors.nickname ? '#dc3545' : duplicateCheck.nickname === true ? '#28a745' : duplicateCheck.nickname === false ? '#dc3545' : '#e9ecef'}`,
                                    borderRadius: '8px',
                                    fontSize: '16px',
                                    outline: 'none'
                                }}
                            />
                            <button
                                type="button"
                                onClick={checkDuplicateNickname}
                                style={{
                                    padding: '12px 16px',
                                    backgroundColor: '#6c757d',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '14px',
                                    cursor: 'pointer',
                                    whiteSpace: 'nowrap'
                                }}
                            >
                                중복확인
                            </button>
                        </div>
                        {errors.nickname && (
                            <div style={{ color: '#dc3545', fontSize: '14px', marginTop: '5px' }}>
                                {errors.nickname}
                            </div>
                        )}
                        {duplicateCheck.nickname === true && (
                            <div style={{ color: '#28a745', fontSize: '14px', marginTop: '5px' }}>
                                ✓ 사용 가능한 닉네임입니다.
                            </div>
                        )}
                    </div>

                    {/* 비밀번호 입력 */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ 
                            display: 'block', 
                            marginBottom: '8px',
                            fontWeight: '600',
                            color: '#495057'
                        }}>
                            비밀번호 *
                        </label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="6자 이상의 비밀번호"
                            required
                            style={{
                                width: '100%',
                                padding: '12px',
                                border: `2px solid ${errors.password ? '#dc3545' : '#e9ecef'}`,
                                borderRadius: '8px',
                                fontSize: '16px',
                                outline: 'none'
                            }}
                        />
                        {errors.password && (
                            <div style={{ color: '#dc3545', fontSize: '14px', marginTop: '5px' }}>
                                {errors.password}
                            </div>
                        )}
                    </div>

                    {/* 비밀번호 확인 입력 */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ 
                            display: 'block', 
                            marginBottom: '8px',
                            fontWeight: '600',
                            color: '#495057'
                        }}>
                            비밀번호 확인 *
                        </label>
                        <input
                            type="password"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            placeholder="비밀번호를 다시 입력하세요"
                            required
                            style={{
                                width: '100%',
                                padding: '12px',
                                border: `2px solid ${errors.confirmPassword ? '#dc3545' : formData.confirmPassword && formData.password === formData.confirmPassword ? '#28a745' : '#e9ecef'}`,
                                borderRadius: '8px',
                                fontSize: '16px',
                                outline: 'none'
                            }}
                        />
                        {errors.confirmPassword && (
                            <div style={{ color: '#dc3545', fontSize: '14px', marginTop: '5px' }}>
                                {errors.confirmPassword}
                            </div>
                        )}
                        {formData.confirmPassword && formData.password === formData.confirmPassword && (
                            <div style={{ color: '#28a745', fontSize: '14px', marginTop: '5px' }}>
                                ✓ 비밀번호가 일치합니다.
                            </div>
                        )}
                    </div>

                    {/* 이메일 입력 (선택사항) */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ 
                            display: 'block', 
                            marginBottom: '8px',
                            fontWeight: '600',
                            color: '#495057'
                        }}>
                            이메일 (선택사항)
                        </label>
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <input
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                placeholder="example@email.com"
                                style={{
                                    flex: 1,
                                    padding: '12px',
                                    border: `2px solid ${errors.email ? '#dc3545' : duplicateCheck.email === true ? '#28a745' : duplicateCheck.email === false ? '#dc3545' : '#e9ecef'}`,
                                    borderRadius: '8px',
                                    fontSize: '16px',
                                    outline: 'none'
                                }}
                            />
                            {formData.email && (
                                <button
                                    type="button"
                                    onClick={checkDuplicateEmail}
                                    style={{
                                        padding: '12px 16px',
                                        backgroundColor: '#6c757d',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '8px',
                                        fontSize: '14px',
                                        cursor: 'pointer',
                                        whiteSpace: 'nowrap'
                                    }}
                                >
                                    중복확인
                                </button>
                            )}
                        </div>
                        {errors.email && (
                            <div style={{ color: '#dc3545', fontSize: '14px', marginTop: '5px' }}>
                                {errors.email}
                            </div>
                        )}
                        {duplicateCheck.email === true && (
                            <div style={{ color: '#28a745', fontSize: '14px', marginTop: '5px' }}>
                                ✓ 사용 가능한 이메일입니다.
                            </div>
                        )}
                    </div>

                    {/* 회원가입 버튼 */}
                    <button
                        type="submit"
                        disabled={loading}
                        style={{
                            width: '100%',
                            padding: '14px',
                            backgroundColor: loading ? '#95a5a6' : '#2ecc71',
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
                            if (!loading) e.target.style.backgroundColor = '#27ae60';
                        }}
                        onMouseLeave={(e) => {
                            if (!loading) e.target.style.backgroundColor = '#2ecc71';
                        }}
                    >
                        {loading ? '가입 중...' : '회원가입'}
                    </button>

                    {/* 로그인 링크 */}
                    <div style={{ textAlign: 'center' }}>
                        <span style={{ color: '#6c757d', fontSize: '14px' }}>
                            이미 계정이 있으신가요?{' '}
                        </span>
                        <Link
                            to="/login"
                            style={{
                                color: '#3498db',
                                textDecoration: 'none',
                                fontWeight: '600',
                                fontSize: '14px'
                            }}
                            onMouseEnter={(e) => e.target.style.textDecoration = 'underline'}
                            onMouseLeave={(e) => e.target.style.textDecoration = 'none'}
                        >
                            로그인
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default SignUp;