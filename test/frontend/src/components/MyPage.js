import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '../contexts/AuthContext';

/**
 * MyPage 컴포넌트
 * 사용자 마이페이지
 * 
 * 주요 기능:
 * - 사용자 정보 표시
 * - 권한 변경 기능 (일반회원 ↔ 회사원 ↔ 관리자)
 * - 프로필 정보 수정
 */
function MyPage() {
    const { user, login } = useAuth();
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    
    // 닉네임 변경 관련 상태
    const [newNickname, setNewNickname] = useState('');
    const [nicknameLoading, setNicknameLoading] = useState(false);
    const [nicknameMessage, setNicknameMessage] = useState('');
    const [nicknameError, setNicknameError] = useState('');
    const [nicknameCheckResult, setNicknameCheckResult] = useState('');

    // 사용자 권한 매핑
    const roleNames = {
        'GENERAL': '일반회원',
        'COMPANY': '회사원', 
        'ADMIN': '관리자'
    };

    const roleColors = {
        'GENERAL': '#95a5a6',
        'COMPANY': '#f39c12',
        'ADMIN': '#e74c3c'
    };

    /**
     * 권한 변경 핸들러
     */
    const handleRoleChange = async (newRole) => {
        if (window.confirm(`권한을 ${roleNames[newRole]}으로 변경하시겠습니까?`)) {
            setLoading(true);
            setError('');
            setMessage('');

            try {
                const response = await axios.put(`http://localhost:8080/api/users/${user.id}/change-role`, {
                    role: newRole
                });

                // AuthContext의 사용자 정보 업데이트
                const updatedUser = { ...user, role: newRole };
                login(updatedUser);

                setMessage(`권한이 ${roleNames[newRole]}으로 변경되었습니다.`);
            } catch (error) {
                console.error('권한 변경 오류:', error);
                setError(error.response?.data?.message || '권한 변경에 실패했습니다.');
            } finally {
                setLoading(false);
            }
        }
    };

    /**
     * 닉네임 중복 체크 함수 (현재 사용자 제외)
     */
    const checkNicknameDuplicate = async (nickname) => {
        if (!nickname || nickname.trim() === '' || nickname === user.nickname) {
            setNicknameCheckResult('');
            return;
        }

        try {
            const response = await axios.post('http://localhost:8080/api/users/check-nickname-exclude', {
                nickname: nickname.trim(),
                userId: user.id
            });
            
            if (response.data.exists) {
                setNicknameCheckResult('이미 사용 중인 닉네임입니다.');
            } else {
                setNicknameCheckResult('사용 가능한 닉네임입니다.');
            }
        } catch (error) {
            console.error('닉네임 중복 체크 오류:', error);
            setNicknameCheckResult('중복 체크 중 오류가 발생했습니다.');
        }
    };

    /**
     * 닉네임 입력 핸들러 (실시간 중복 체크)
     */
    const handleNicknameChange = (e) => {
        const value = e.target.value;
        setNewNickname(value);
        setNicknameError('');
        setNicknameMessage('');
        
        // 디바운싱을 위한 타이머 설정
        clearTimeout(window.nicknameCheckTimer);
        window.nicknameCheckTimer = setTimeout(() => {
            checkNicknameDuplicate(value);
        }, 500);
    };

    /**
     * 닉네임 변경 핸들러
     */
    const handleNicknameSubmit = async (e) => {
        e.preventDefault();
        
        if (!newNickname || newNickname.trim() === '') {
            setNicknameError('닉네임을 입력해주세요.');
            return;
        }

        if (newNickname.trim() === user.nickname) {
            setNicknameError('현재 닉네임과 동일합니다.');
            return;
        }

        if (nicknameCheckResult === '이미 사용 중인 닉네임입니다.') {
            setNicknameError('다른 닉네임을 입력해주세요.');
            return;
        }

        setNicknameLoading(true);
        setNicknameError('');
        setNicknameMessage('');

        try {
            const response = await axios.put(`http://localhost:8080/api/users/${user.id}/change-nickname`, {
                nickname: newNickname.trim()
            });

            // AuthContext의 사용자 정보 업데이트
            const updatedUser = { ...user, nickname: newNickname.trim() };
            login(updatedUser);

            setNicknameMessage('닉네임이 성공적으로 변경되었습니다.');
            setNewNickname('');
            setNicknameCheckResult('');
        } catch (error) {
            console.error('닉네임 변경 오류:', error);
            setNicknameError(error.response?.data?.message || '닉네임 변경에 실패했습니다.');
        } finally {
            setNicknameLoading(false);
        }
    };

    if (!user) {
        return (
            <div style={{ 
                minHeight: '80vh', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center' 
            }}>
                <div style={{ textAlign: 'center' }}>
                    <h2>로그인이 필요합니다</h2>
                    <p>마이페이지를 이용하려면 먼저 로그인해주세요.</p>
                </div>
            </div>
        );
    }

    return (
        <div style={{ 
            minHeight: '80vh', 
            backgroundColor: '#f8f9fa', 
            padding: '40px 20px' 
        }}>
            <div style={{ 
                maxWidth: '800px', 
                margin: '0 auto' 
            }}>
                {/* 페이지 헤더 */}
                <div style={{ 
                    backgroundColor: 'white',
                    borderRadius: '12px',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                    padding: '30px',
                    marginBottom: '30px'
                }}>
                    <h1 style={{ 
                        color: '#2c3e50', 
                        marginBottom: '10px',
                        fontSize: '32px',
                        fontWeight: 'bold',
                        textAlign: 'center'
                    }}>
                        👤 마이페이지
                    </h1>
                    <p style={{ 
                        color: '#6c757d', 
                        fontSize: '16px',
                        textAlign: 'center',
                        margin: 0 
                    }}>
                        사용자 정보 및 권한 관리
                    </p>
                </div>

                {/* 사용자 정보 카드 */}
                <div style={{ 
                    backgroundColor: 'white',
                    borderRadius: '12px',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                    padding: '30px',
                    marginBottom: '30px'
                }}>
                    <h2 style={{ 
                        color: '#2c3e50', 
                        marginBottom: '20px',
                        fontSize: '24px',
                        fontWeight: 'bold'
                    }}>
                        📋 내 정보
                    </h2>

                    <div style={{ 
                        display: 'grid', 
                        gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                        gap: '20px'
                    }}>
                        <div style={{ 
                            padding: '20px',
                            backgroundColor: '#f8f9fa',
                            borderRadius: '8px',
                            border: '1px solid #e9ecef'
                        }}>
                            <div style={{ 
                                color: '#6c757d', 
                                fontSize: '14px', 
                                fontWeight: '600',
                                marginBottom: '5px'
                            }}>
                                ID
                            </div>
                            <div style={{ 
                                color: '#2c3e50', 
                                fontSize: '18px', 
                                fontWeight: 'bold'
                            }}>
                                {user.username}
                            </div>
                        </div>

                        <div style={{ 
                            padding: '20px',
                            backgroundColor: '#f8f9fa',
                            borderRadius: '8px',
                            border: '1px solid #e9ecef'
                        }}>
                            <div style={{ 
                                color: '#6c757d', 
                                fontSize: '14px', 
                                fontWeight: '600',
                                marginBottom: '5px'
                            }}>
                                닉네임
                            </div>
                            <div style={{ 
                                color: '#2c3e50', 
                                fontSize: '18px', 
                                fontWeight: 'bold'
                            }}>
                                {user.nickname}
                            </div>
                        </div>

                        <div style={{ 
                            padding: '20px',
                            backgroundColor: '#f8f9fa',
                            borderRadius: '8px',
                            border: '1px solid #e9ecef'
                        }}>
                            <div style={{ 
                                color: '#6c757d', 
                                fontSize: '14px', 
                                fontWeight: '600',
                                marginBottom: '5px'
                            }}>
                                현재 권한
                            </div>
                            <div style={{ 
                                color: roleColors[user.role], 
                                fontSize: '18px', 
                                fontWeight: 'bold'
                            }}>
                                {roleNames[user.role]}
                            </div>
                        </div>

                        {user.email && (
                            <div style={{ 
                                padding: '20px',
                                backgroundColor: '#f8f9fa',
                                borderRadius: '8px',
                                border: '1px solid #e9ecef'
                            }}>
                                <div style={{ 
                                    color: '#6c757d', 
                                    fontSize: '14px', 
                                    fontWeight: '600',
                                    marginBottom: '5px'
                                }}>
                                    이메일
                                </div>
                                <div style={{ 
                                    color: '#2c3e50', 
                                    fontSize: '18px', 
                                    fontWeight: 'bold'
                                }}>
                                    {user.email}
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* 닉네임 변경 카드 */}
                <div style={{ 
                    backgroundColor: 'white',
                    borderRadius: '12px',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                    padding: '30px',
                    marginBottom: '30px'
                }}>
                    <h2 style={{ 
                        color: '#2c3e50', 
                        marginBottom: '20px',
                        fontSize: '24px',
                        fontWeight: 'bold'
                    }}>
                        ✏️ 닉네임 변경
                    </h2>

                    {/* 닉네임 변경 메시지 */}
                    {nicknameMessage && (
                        <div style={{
                            backgroundColor: '#d4edda',
                            color: '#155724',
                            padding: '12px',
                            borderRadius: '6px',
                            marginBottom: '20px',
                            border: '1px solid #c3e6cb'
                        }}>
                            {nicknameMessage}
                        </div>
                    )}

                    {nicknameError && (
                        <div style={{
                            backgroundColor: '#f8d7da',
                            color: '#721c24',
                            padding: '12px',
                            borderRadius: '6px',
                            marginBottom: '20px',
                            border: '1px solid #f5c6cb'
                        }}>
                            {nicknameError}
                        </div>
                    )}

                    <div style={{ 
                        display: 'flex',
                        alignItems: 'center',
                        gap: '15px',
                        marginBottom: '15px',
                        flexWrap: 'wrap'
                    }}>
                        <div style={{
                            fontSize: '16px',
                            color: '#6c757d',
                            fontWeight: '600'
                        }}>
                            현재 닉네임: <span style={{ color: '#2c3e50', fontWeight: 'bold' }}>{user.nickname}</span>
                        </div>
                    </div>

                    <form onSubmit={handleNicknameSubmit}>
                        <div style={{ 
                            display: 'flex',
                            gap: '15px',
                            alignItems: 'flex-start',
                            flexWrap: 'wrap'
                        }}>
                            <div style={{ flex: '1', minWidth: '250px' }}>
                                <input
                                    type="text"
                                    value={newNickname}
                                    onChange={handleNicknameChange}
                                    placeholder="새로운 닉네임을 입력하세요"
                                    disabled={nicknameLoading}
                                    style={{
                                        width: '100%',
                                        padding: '15px',
                                        fontSize: '16px',
                                        border: '2px solid #e9ecef',
                                        borderRadius: '8px',
                                        outline: 'none',
                                        transition: 'border-color 0.3s',
                                        backgroundColor: nicknameLoading ? '#f8f9fa' : 'white'
                                    }}
                                    onFocus={(e) => e.target.style.borderColor = '#007bff'}
                                    onBlur={(e) => e.target.style.borderColor = '#e9ecef'}
                                />
                                
                                {/* 중복 체크 결과 */}
                                {nicknameCheckResult && (
                                    <div style={{
                                        marginTop: '8px',
                                        fontSize: '14px',
                                        color: nicknameCheckResult === '사용 가능한 닉네임입니다.' ? '#28a745' : '#dc3545',
                                        fontWeight: '600'
                                    }}>
                                        {nicknameCheckResult === '사용 가능한 닉네임입니다.' ? '✓ ' : '✗ '}
                                        {nicknameCheckResult}
                                    </div>
                                )}
                            </div>

                            <button
                                type="submit"
                                disabled={nicknameLoading || !newNickname.trim() || newNickname.trim() === user.nickname || nicknameCheckResult === '이미 사용 중인 닉네임입니다.'}
                                style={{
                                    padding: '15px 25px',
                                    backgroundColor: nicknameLoading ? '#95a5a6' : '#007bff',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '16px',
                                    fontWeight: '600',
                                    cursor: nicknameLoading ? 'not-allowed' : 'pointer',
                                    transition: 'all 0.3s',
                                    minWidth: '120px'
                                }}
                                onMouseEnter={(e) => {
                                    if (!nicknameLoading && !e.target.disabled) {
                                        e.target.style.backgroundColor = '#0056b3';
                                        e.target.style.transform = 'translateY(-1px)';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (!nicknameLoading && !e.target.disabled) {
                                        e.target.style.backgroundColor = '#007bff';
                                        e.target.style.transform = 'translateY(0)';
                                    }
                                }}
                            >
                                {nicknameLoading ? '변경 중...' : '닉네임 변경'}
                            </button>
                        </div>
                    </form>

                    <div style={{
                        marginTop: '20px',
                        padding: '15px',
                        backgroundColor: '#fff3cd',
                        borderRadius: '8px',
                        border: '1px solid #ffeaa7'
                    }}>
                        <div style={{ 
                            color: '#856404', 
                            fontSize: '14px',
                            fontWeight: '600'
                        }}>
                            💡 닉네임 변경 시 주의사항
                        </div>
                        <ul style={{ 
                            color: '#856404', 
                            fontSize: '13px',
                            marginTop: '8px',
                            marginLeft: '16px',
                            paddingLeft: '0'
                        }}>
                            <li>다른 사용자가 사용 중인 닉네임은 선택할 수 없습니다</li>
                            <li>닉네임 변경 후 작성한 게시글과 댓글의 작성자명이 바뀝니다</li>
                            <li>특수문자와 공백은 피해주세요</li>
                        </ul>
                    </div>
                </div>

                {/* 권한 변경 카드 */}
                <div style={{ 
                    backgroundColor: 'white',
                    borderRadius: '12px',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                    padding: '30px'
                }}>
                    <h2 style={{ 
                        color: '#2c3e50', 
                        marginBottom: '20px',
                        fontSize: '24px',
                        fontWeight: 'bold'
                    }}>
                        🔄 권한 변경
                    </h2>

                    {/* 메시지 표시 */}
                    {message && (
                        <div style={{
                            backgroundColor: '#d4edda',
                            color: '#155724',
                            padding: '12px',
                            borderRadius: '6px',
                            marginBottom: '20px',
                            border: '1px solid #c3e6cb'
                        }}>
                            {message}
                        </div>
                    )}

                    {error && (
                        <div style={{
                            backgroundColor: '#f8d7da',
                            color: '#721c24',
                            padding: '12px',
                            borderRadius: '6px',
                            marginBottom: '20px',
                            border: '1px solid #f5c6cb'
                        }}>
                            {error}
                        </div>
                    )}

                    <p style={{ 
                        color: '#6c757d', 
                        marginBottom: '25px',
                        fontSize: '16px'
                    }}>
                        원하시는 권한으로 변경할 수 있습니다. (데모 목적)
                    </p>

                    <div style={{ 
                        display: 'grid', 
                        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                        gap: '15px'
                    }}>
                        {['GENERAL', 'COMPANY', 'ADMIN'].map(role => (
                            <button
                                key={role}
                                onClick={() => handleRoleChange(role)}
                                disabled={loading || user.role === role}
                                style={{
                                    padding: '15px 20px',
                                    backgroundColor: user.role === role ? '#95a5a6' : roleColors[role],
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '16px',
                                    fontWeight: '600',
                                    cursor: user.role === role || loading ? 'not-allowed' : 'pointer',
                                    transition: 'all 0.3s',
                                    opacity: user.role === role ? 0.6 : 1
                                }}
                                onMouseEnter={(e) => {
                                    if (user.role !== role && !loading) {
                                        e.target.style.transform = 'translateY(-2px)';
                                        e.target.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.2)';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (user.role !== role && !loading) {
                                        e.target.style.transform = 'translateY(0)';
                                        e.target.style.boxShadow = 'none';
                                    }
                                }}
                            >
                                {user.role === role ? 
                                    `✓ 현재 권한: ${roleNames[role]}` : 
                                    `${roleNames[role]}으로 변경`
                                }
                            </button>
                        ))}
                    </div>

                    {loading && (
                        <div style={{ 
                            textAlign: 'center', 
                            marginTop: '20px',
                            color: '#6c757d'
                        }}>
                            권한 변경 중...
                        </div>
                    )}

                    <div style={{
                        marginTop: '25px',
                        padding: '15px',
                        backgroundColor: '#e3f2fd',
                        borderRadius: '8px',
                        border: '1px solid #bbdefb'
                    }}>
                        <h4 style={{ 
                            color: '#1565c0', 
                            marginBottom: '10px',
                            fontSize: '16px'
                        }}>
                            ℹ️ 권한별 접근 범위
                        </h4>
                        <ul style={{ 
                            color: '#1976d2', 
                            fontSize: '14px',
                            marginLeft: '20px'
                        }}>
                            <li><strong>일반회원:</strong> 자유게시판, Q&A 읽기/쓰기 + 공지사항 읽기</li>
                            <li><strong>회사원:</strong> 일반회원 권한 + 기업게시판 읽기/쓰기</li>
                            <li><strong>관리자:</strong> 모든 게시판 읽기/쓰기 권한</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default MyPage;