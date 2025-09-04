// React 라이브러리 임포트
import React from 'react';

/**
 * Pagination 컴포넌트
 * 게시글 목록 등에서 사용되는 페이지네이션 UI 컴포넌트
 * 
 * Props:
 * - currentPage: 현재 페이지 번호 (0부터 시작)
 * - totalPages: 전체 페이지 수
 * - onPageChange: 페이지 변경 시 호출될 콜백 함수
 * - hasNext: 다음 페이지 존재 여부
 * - hasPrevious: 이전 페이지 존재 여부
 * 
 * 기능:
 * - 현재 페이지 중심으로 최대 5개 페이지 번호 표시
 * - 이전/다음 버튼 제공
 * - 반응형 디자인과 호버 효과
 */
function Pagination({ currentPage, totalPages, onPageChange, hasNext, hasPrevious }) {
  // 표시할 페이지 번호들을 저장할 배열
  const pageNumbers = [];
  // 한 번에 보여줄 최대 페이지 버튼 수
  const maxVisiblePages = 5;
  
  // === 페이지 번호 범위 계산 로직 ===
  
  // 현재 페이지 중심으로 시작 페이지 계산
  let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
  // 끝 페이지 계산
  let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
  
  // 전체 페이지 수가 maxVisiblePages보다 적을 때 범위 조정
  if (endPage - startPage + 1 < maxVisiblePages) {
    startPage = Math.max(0, endPage - maxVisiblePages + 1);
  }
  
  // 계산된 범위로 페이지 번호 배열 생성
  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  // 총 페이지가 0이면 페이지네이션 숨김
  if (totalPages === 0) return null;

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      gap: '8px', 
      marginTop: '30px',
      padding: '20px'
    }}>
      {/* 첫 페이지로 */}
      {currentPage > 1 && (
        <button
          onClick={() => onPageChange(0)}
          style={{
            padding: '8px 12px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            backgroundColor: 'white',
            color: '#666',
            cursor: 'pointer',
            fontSize: '14px'
          }}
          onMouseEnter={(e) => {
            e.target.style.backgroundColor = '#f8f9fa';
          }}
          onMouseLeave={(e) => {
            e.target.style.backgroundColor = 'white';
          }}
        >
          ««
        </button>
      )}
      
      {/* 이전 페이지 */}
      {hasPrevious && (
        <button
          onClick={() => onPageChange(currentPage - 1)}
          style={{
            padding: '8px 12px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            backgroundColor: 'white',
            color: '#666',
            cursor: 'pointer',
            fontSize: '14px'
          }}
          onMouseEnter={(e) => {
            e.target.style.backgroundColor = '#f8f9fa';
          }}
          onMouseLeave={(e) => {
            e.target.style.backgroundColor = 'white';
          }}
        >
          ‹
        </button>
      )}

      {/* 페이지 번호들 */}
      {pageNumbers.map(pageNum => (
        <button
          key={pageNum}
          onClick={() => onPageChange(pageNum)}
          style={{
            padding: '8px 12px',
            border: currentPage === pageNum ? '2px solid #3498db' : '1px solid #ddd',
            borderRadius: '4px',
            backgroundColor: currentPage === pageNum ? '#3498db' : 'white',
            color: currentPage === pageNum ? 'white' : '#666',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: currentPage === pageNum ? '600' : '400',
            minWidth: '40px'
          }}
          onMouseEnter={(e) => {
            if (currentPage !== pageNum) {
              e.target.style.backgroundColor = '#f8f9fa';
            }
          }}
          onMouseLeave={(e) => {
            if (currentPage !== pageNum) {
              e.target.style.backgroundColor = 'white';
            }
          }}
        >
          {pageNum + 1}
        </button>
      ))}

      {/* 다음 페이지 */}
      {hasNext && (
        <button
          onClick={() => onPageChange(currentPage + 1)}
          style={{
            padding: '8px 12px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            backgroundColor: 'white',
            color: '#666',
            cursor: 'pointer',
            fontSize: '14px'
          }}
          onMouseEnter={(e) => {
            e.target.style.backgroundColor = '#f8f9fa';
          }}
          onMouseLeave={(e) => {
            e.target.style.backgroundColor = 'white';
          }}
        >
          ›
        </button>
      )}

      {/* 마지막 페이지로 */}
      {currentPage < totalPages - 2 && (
        <button
          onClick={() => onPageChange(totalPages - 1)}
          style={{
            padding: '8px 12px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            backgroundColor: 'white',
            color: '#666',
            cursor: 'pointer',
            fontSize: '14px'
          }}
          onMouseEnter={(e) => {
            e.target.style.backgroundColor = '#f8f9fa';
          }}
          onMouseLeave={(e) => {
            e.target.style.backgroundColor = 'white';
          }}
        >
          »»
        </button>
      )}
    </div>
  );
}

export default Pagination;