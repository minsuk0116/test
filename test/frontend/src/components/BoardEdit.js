// React 라이브러리와 필요한 훅들 임포트
import React, { useEffect, useState } from 'react';
// HTTP 요청을 위한 axios 라이브러리
import axios from 'axios';
// React Router 훅들 - URL 파라미터와 페이지 이동용
import { useParams, useNavigate } from 'react-router-dom';

/**
 * BoardEdit 컴포넌트
 * 기존 게시글을 수정하는 페이지
 * 
 * 주요 기능:
 * - 기존 게시글 데이터 로딩 및 폼에 채우기
 * - 게시글 제목, 내용, 타입 수정
 * - 수정된 데이터를 서버에 전송
 * - 수정 완료 후 상세 페이지로 이동
 */
function BoardEdit() {
    // URL에서 게시글 ID 추출 (예: /boards/edit/123 → id = "123")
    const { id } = useParams();
    
    // 프로그래밍적 페이지 이동을 위한 훅
    const navigate = useNavigate();

    // === 상태 변수들 정의 ===
    // 게시글 제목 상태 (수정 가능)
    const [title, setTitle] = useState('');
    
    // 게시글 내용 상태 (수정 가능)  
    const [content, setContent] = useState('');
    
    // 게시판 타입 상태 (수정 가능)
    const [boardType, setBoardType] = useState('FREE');

    /**
     * useEffect: 컴포넌트 마운트 시 기존 게시글 데이터 로딩
     * 서버에서 현재 게시글 정보를 가져와서 입력 폼에 채움
     */
    useEffect(() => {
        // 특정 게시글 데이터 요청
        axios.get(`http://localhost:8080/boards/${id}`)
            .then(Response => {
                const board = Response.data;
                // 서버에서 받은 데이터로 각 상태 초기화
                setTitle(board.title);          // 기존 제목으로 설정
                setContent(board.content);      // 기존 내용으로 설정
                setBoardType(board.boardType);  // 기존 게시판 타입으로 설정
            })
            .catch(error => console.error('Error fetching board:', error));
    }, [id]); // id가 변경되면 새로 데이터를 가져옴

    /**
     * 폼 제출 핸들러 함수
     * 수정된 데이터를 서버에 전송하여 게시글 업데이트
     */
    const handleSubmit = (e) => {
        // 폼의 기본 제출 동작 방지
        e.preventDefault();
        
        // 서버에 전송할 수정된 데이터 객체 생성
        const data = { title, content, boardType };

        // PUT 요청으로 게시글 수정 (전체 데이터 교체)
        axios.put(`http://localhost:8080/boards/${id}`, data)
            .then(() => {
                // 성공시 알림 후 해당 게시글 상세 페이지로 이동
                alert('게시글이 수정되었습니다.');
                navigate(`/boards/${id}`);
            })
            .catch(error => {
                // 실패시 에러 로그 및 사용자 알림
                console.error('게시글 수정 실패:', error);
                alert('수정 중 오류가 발생했습니다.');
            });
    };


    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <h2>게시글 수정</h2>
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>제목:</label>
                    <input
                        type="text"
                        value={title}
                        onChange={e => setTitle(e.target.value)}
                        required
                        style={{ 
                            width: '100%', 
                            padding: '8px', 
                            border: '1px solid #ddd',
                            borderRadius: '4px'
                        }}
                    />
                </div>

                <div style={{ marginBottom: '15px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>내용:</label>
                    <textarea
                        value={content}
                        onChange={e => setContent(e.target.value)}
                        required
                        rows="10"
                        style={{ 
                            width: '100%', 
                            padding: '8px', 
                            border: '1px solid #ddd',
                            borderRadius: '4px'
                        }}
                    />
                </div>

                <div style={{ marginBottom: '20px' }}>
                    <label style={{ display: 'block', marginBottom: '5px' }}>게시판 종류:</label>
                    <select 
                        value={boardType} 
                        onChange={e => setBoardType(e.target.value)}
                        style={{ 
                            padding: '8px', 
                            border: '1px solid #ddd',
                            borderRadius: '4px'
                        }}
                    >
                        <option value="NOTICE">📢 공지사항</option>
                        <option value="COMPANY">🏢 기업게시판</option>
                        <option value="FREE">💬 자유게시판</option>
                        <option value="QNA">❓ Q&A</option>
                    </select>
                </div>

                <div style={{ textAlign: 'center' }}>
                    <button 
                        type="submit"
                        style={{ 
                            backgroundColor: '#2ecc71', 
                            color: 'white', 
                            padding: '10px 20px',
                            border: 'none',
                            borderRadius: '4px',
                            marginRight: '10px',
                            cursor: 'pointer'
                        }}
                    >
                        수정 완료
                    </button>
                    <button 
                        type="button"
                        onClick={() => navigate(`/boards/${id}`)}
                        style={{ 
                            backgroundColor: '#95a5a6', 
                            color: 'white', 
                            padding: '10px 20px',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer'
                        }}
                    >
                        취소
                    </button>
                </div>
            </form>
        </div>
    )
}

export default BoardEdit;