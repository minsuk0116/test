// React 라이브러리와 필요한 훅들 임포트
import React, { useState, useEffect } from 'react';
// HTTP 요청을 위한 axios 라이브러리
import axios from 'axios';
// 사용자 인증 정보를 위한 AuthContext
import { useAuth } from '../contexts/AuthContext';

/**
 * CommentSection 컴포넌트
 * 게시글에 대한 댓글 시스템을 구현하는 컴포넌트
 * 
 * Props:
 * - boardId: 댓글이 속할 게시글의 ID
 * 
 * 주요 기능:
 * - 댓글 목록 조회 및 표시
 * - 새 댓글 작성
 * - 댓글에 대한 답글(대댓글) 작성
 * - 댓글 수정 및 삭제
 * - 계층형 댓글 구조 지원
 */
function CommentSection({ boardId }) {
  // 로그인한 사용자 정보 가져오기
  const { user } = useAuth();
  
  // === 상태 변수들 정의 ===
  
  // 댓글 목록을 저장하는 배열 상태
  const [comments, setComments] = useState([]);
  
  // 새 댓글 작성용 상태들
  const [newComment, setNewComment] = useState('');  // 새 댓글 내용
  
  // 답글 작성용 상태들
  const [replyingTo, setReplyingTo] = useState(null);  // 답글을 작성 중인 댓글 ID
  const [replyContent, setReplyContent] = useState(''); // 답글 내용
  
  // 댓글 수정용 상태들
  const [editingComment, setEditingComment] = useState(null); // 수정 중인 댓글 ID
  const [editContent, setEditContent] = useState('');         // 수정될 댓글 내용

  /**
   * useEffect: 컴포넌트 마운트 시 또는 boardId 변경 시 댓글 목록 로딩
   * 게시글이 바뀔 때마다 해당 게시글의 댓글들을 새로 불러옴
   */
  useEffect(() => {
    fetchComments();
  }, [boardId]); // boardId가 변경될 때마다 실행

  /**
   * 댓글 목록을 서버에서 가져오는 비동기 함수
   * 특정 게시글의 모든 댓글을 조회
   */
  const fetchComments = async () => {
    try {
      // 게시글 ID를 기준으로 댓글 목록 요청
      const response = await axios.get(`http://localhost:8080/boards/${boardId}/comments`);
      setComments(response.data); // 받아온 댓글 목록을 상태에 저장
    } catch (error) {
      console.error('댓글 조회 실패:', error);
    }
  };

  /**
   * 새 댓글 작성 핸들러 함수
   * 사용자가 입력한 댓글을 서버에 전송하여 저장
   */
  const handleSubmitComment = async (e) => {
    // 폼의 기본 제출 동작 방지
    e.preventDefault();
    
    // 로그인 확인
    if (!user) {
      alert('댓글 작성을 위해 로그인이 필요합니다.');
      return;
    }
    
    // 빈 내용 검증
    if (!newComment.trim()) return;

    try {
      // POST 요청으로 새 댓글 생성 (authorId를 URL 파라미터로 전송)
      await axios.post(`http://localhost:8080/boards/${boardId}/comments?authorId=${user.id}`, {
        content: newComment,   // 댓글 내용
        parentId: null        // 최상위 댓글이므로 부모 ID는 null
      });
      
      // 입력 필드 초기화
      setNewComment('');
      
      // 댓글 목록 새로고침 (새 댓글이 목록에 반영되도록)
      fetchComments();
    } catch (error) {
      console.error('댓글 작성 실패:', error);
      alert('댓글 작성에 실패했습니다.');
    }
  };

  /**
   * 답글(대댓글) 작성 핸들러 함수
   * 특정 댓글에 대한 답글을 서버에 전송하여 저장
   */
  const handleSubmitReply = async (e, parentId) => {
    e.preventDefault();
    
    // 로그인 확인
    if (!user) {
      alert('댓글 작성을 위해 로그인이 필요합니다.');
      return;
    }
    
    // 빈 내용 검증
    if (!replyContent.trim()) return;

    try {
      await axios.post(`http://localhost:8080/boards/${boardId}/comments?authorId=${user.id}`, {
        content: replyContent,
        parentId: parentId
      });
      setReplyContent('');
      setReplyingTo(null);
      fetchComments();
    } catch (error) {
      console.error('답글 작성 실패:', error);
      alert('답글 작성에 실패했습니다.');
    }
  };

  const handleEditComment = async (commentId) => {
    if (!editContent.trim()) return;

    try {
      await axios.put(`http://localhost:8080/boards/${boardId}/comments/${commentId}`, {
        content: editContent
      });
      setEditingComment(null);
      setEditContent('');
      fetchComments();
    } catch (error) {
      console.error('댓글 수정 실패:', error);
      alert('댓글 수정에 실패했습니다.');
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('댓글을 삭제하시겠습니까?')) return;

    try {
      await axios.delete(`http://localhost:8080/boards/${boardId}/comments/${commentId}`);
      fetchComments();
    } catch (error) {
      console.error('댓글 삭제 실패:', error);
      alert('댓글 삭제에 실패했습니다.');
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const renderComment = (comment, depth = 0) => (
    <div key={comment.id} style={{ marginLeft: `${depth * 20}px`, marginBottom: '15px' }}>
      <div style={{
        backgroundColor: depth > 0 ? '#f8f9fa' : 'white',
        border: '1px solid #e9ecef',
        borderRadius: '8px',
        padding: '15px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <strong style={{ color: '#2c3e50' }}>{comment.author}</strong>
            <span style={{ fontSize: '12px', color: '#6c757d' }}>
              {formatDate(comment.createdAt)}
            </span>
            {comment.updatedAt !== comment.createdAt && (
              <span style={{ fontSize: '11px', color: '#adb5bd' }}>(수정됨)</span>
            )}
          </div>
          <div style={{ display: 'flex', gap: '5px' }}>
            <button
              onClick={() => setReplyingTo(replyingTo === comment.id ? null : comment.id)}
              style={{
                fontSize: '12px',
                padding: '4px 8px',
                border: 'none',
                borderRadius: '4px',
                backgroundColor: '#e3f2fd',
                color: '#1976d2',
                cursor: 'pointer'
              }}
            >
              답글
            </button>
            <button
              onClick={() => {
                setEditingComment(comment.id);
                setEditContent(comment.content);
              }}
              style={{
                fontSize: '12px',
                padding: '4px 8px',
                border: 'none',
                borderRadius: '4px',
                backgroundColor: '#fff3e0',
                color: '#f57c00',
                cursor: 'pointer'
              }}
            >
              수정
            </button>
            <button
              onClick={() => handleDeleteComment(comment.id)}
              style={{
                fontSize: '12px',
                padding: '4px 8px',
                border: 'none',
                borderRadius: '4px',
                backgroundColor: '#ffebee',
                color: '#d32f2f',
                cursor: 'pointer'
              }}
            >
              삭제
            </button>
          </div>
        </div>

        {editingComment === comment.id ? (
          <div>
            <textarea
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              style={{
                width: '100%',
                minHeight: '60px',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px',
                resize: 'vertical'
              }}
            />
            <div style={{ marginTop: '8px', display: 'flex', gap: '5px' }}>
              <button
                onClick={() => handleEditComment(comment.id)}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#28a745',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  fontSize: '12px',
                  cursor: 'pointer'
                }}
              >
                수정완료
              </button>
              <button
                onClick={() => {
                  setEditingComment(null);
                  setEditContent('');
                }}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  fontSize: '12px',
                  cursor: 'pointer'
                }}
              >
                취소
              </button>
            </div>
          </div>
        ) : (
          <p style={{ margin: '0', lineHeight: '1.5', color: '#495057' }}>
            {comment.content}
          </p>
        )}

        {replyingTo === comment.id && (
          <form onSubmit={(e) => handleSubmitReply(e, comment.id)} style={{ marginTop: '15px', padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
            <div style={{ marginBottom: '8px', fontSize: '14px', color: '#666' }}>
              답글 작성자: <strong>{user?.nickname || '로그인 필요'}</strong>
            </div>
            <textarea
              placeholder="답글을 입력하세요..."
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              style={{
                width: '100%',
                minHeight: '60px',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px',
                resize: 'vertical'
              }}
            />
            <div style={{ marginTop: '8px', display: 'flex', gap: '5px' }}>
              <button
                type="submit"
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  fontSize: '12px',
                  cursor: 'pointer'
                }}
              >
                답글 작성
              </button>
              <button
                type="button"
                onClick={() => {
                  setReplyingTo(null);
                  setReplyContent('');
                }}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  fontSize: '12px',
                  cursor: 'pointer'
                }}
              >
                취소
              </button>
            </div>
          </form>
        )}
      </div>

      {comment.children && comment.children.length > 0 && (
        <div style={{ marginTop: '10px' }}>
          {comment.children.map(child => renderComment(child, depth + 1))}
        </div>
      )}
    </div>
  );

  return (
    <div style={{ marginTop: '30px' }}>
      <h3 style={{ marginBottom: '20px', color: '#2c3e50' }}>댓글 ({comments.length})</h3>

      {/* 새 댓글 작성 폼 */}
      <form onSubmit={handleSubmitComment} style={{ marginBottom: '30px', padding: '20px', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
        <div style={{ marginBottom: '10px', fontSize: '14px', color: '#666' }}>
          작성자: <strong>{user?.nickname || '로그인 필요'}</strong>
        </div>
        <textarea
          placeholder="댓글을 입력하세요..."
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          required
          style={{
            width: '100%',
            minHeight: '80px',
            padding: '10px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            fontSize: '14px',
            resize: 'vertical'
          }}
        />
        <button
          type="submit"
          style={{
            marginTop: '10px',
            padding: '10px 20px',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            fontSize: '14px',
            cursor: 'pointer'
          }}
        >
          댓글 작성
        </button>
      </form>

      {/* 댓글 목록 */}
      <div>
        {comments.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#6c757d' }}>
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>💬</div>
            <div>첫 번째 댓글을 작성해보세요!</div>
          </div>
        ) : (
          comments.map(comment => renderComment(comment))
        )}
      </div>
    </div>
  );
}

export default CommentSection;