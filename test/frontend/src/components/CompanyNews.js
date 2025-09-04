// React 라이브러리와 필요한 훅들 임포트
import React, { useState, useEffect } from 'react';
// HTTP 요청을 위한 axios 라이브러리
import axios from 'axios';
// 페이지네이션 컴포넌트
import Pagination from './Pagination';

/**
 * CompanyNews 컴포넌트
 * 기업 뉴스를 표시하고 검색하는 페이지
 * 
 * 주요 기능:
 * - 기본 뉴스 목록 로딩 및 표시
 * - 뉴스 검색 기능
 * - 검색 결과 페이지네이션
 * - 에러 처리 및 로딩 상태 관리
 * - 뉴스 링크 외부 열기
 */
const CompanyNews = () => {
  // === 상태 변수들 정의 ===
  
  // 기본 뉴스 목록 (검색하지 않았을 때 보여지는 뉴스)
  const [defaultNews, setDefaultNews] = useState([]);
  
  // 검색 결과 뉴스 목록 (null이면 기본 뉴스 표시)
  const [searchResults, setSearchResults] = useState(null);
  
  // 사용자 입력 검색어
  const [searchQuery, setSearchQuery] = useState('');
  
  // 데이터 로딩 상태 (스피너 표시용)
  const [loading, setLoading] = useState(false);
  
  // 페이지네이션 관련 상태들
  const [currentPage, setCurrentPage] = useState(0);    // 현재 페이지
  const [totalPages, setTotalPages] = useState(0);      // 전체 페이지 수
  const [hasNext, setHasNext] = useState(false);        // 다음 페이지 존재 여부
  const [hasPrevious, setHasPrevious] = useState(false); // 이전 페이지 존재 여부
  
  // 에러 상태 (에러 메시지 저장)
  const [error, setError] = useState(null);

  /**
   * useEffect: 컴포넌트 마운트 시 기본 뉴스 로드
   * 페이지가 처음 로드될 때 기본 뉴스들을 가져옴
   */
  useEffect(() => {
    fetchDefaultNews();
  }, []); // 빈 의존성 배열 = 컴포넌트 마운트 시에만 실행

  /**
   * 기본 뉴스 데이터를 서버에서 가져오는 비동기 함수
   * 검색하지 않았을 때 기본적으로 보여줄 뉴스들을 로딩
   */
  const fetchDefaultNews = async () => {
    setLoading(true);  // 로딩 상태 시작
    setError(null);    // 이전 에러 메시지 초기화
    
    try {
      // 기본 뉴스 API 호출
      const response = await axios.get('http://localhost:8080/api/news/default');
      setDefaultNews(response.data || []); // 응답 데이터를 상태에 저장
    } catch (error) {
      console.error('기본 뉴스 로딩 실패:', error);
      setError('뉴스를 불러오는데 실패했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setLoading(false); // 성공/실패 관계없이 로딩 상태 종료
    }
  };

  /**
   * 검색 폼 제출 핸들러
   * 사용자가 검색 버튼을 클릭하거나 엔터키를 눌렀을 때 실행
   */
  const handleSearch = async (e) => {
    e.preventDefault(); // 폼의 기본 제출 동작 방지
    
    // 빈 검색어 체크
    if (!searchQuery.trim()) return;

    // 검색 시 항상 첫 페이지부터 시작
    setCurrentPage(0);
    await searchWithPaging(searchQuery, 0);
  };

  /**
   * 페이지네이션이 포함된 뉴스 검색 함수
   * 특정 페이지의 검색 결과를 가져옴
   */
  const searchWithPaging = async (query, page) => {
    setLoading(true);
    setError(null);
    
    try {
      // URL 인코딩으로 특수문자 처리 + 페이지네이션 파라미터
      const response = await axios.get(`http://localhost:8080/api/news/search/page?query=${encodeURIComponent(query)}&page=${page}&size=10`);
      const data = response.data;
      
      if (data && data.news) {
        setSearchResults({
          query: data.query || query,
          totalResults: data.totalElements || 0,
          news: data.news || []
        });
        setCurrentPage(data.currentPage || 0);
        setTotalPages(data.totalPages || 0);
        setHasNext(data.hasNext || false);
        setHasPrevious(data.hasPrevious || false);
      } else {
        setSearchResults({
          query: query,
          totalResults: 0,
          news: []
        });
        setError('검색 결과가 없습니다.');
      }
    } catch (error) {
      console.error('뉴스 검색 실패:', error);
      setError('검색에 실패했습니다. 네트워크 연결을 확인하고 다시 시도해주세요.');
      setSearchResults({
        query: query,
        totalResults: 0,
        news: []
      });
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    if (searchQuery.trim()) {
      searchWithPaging(searchQuery, newPage);
    }
  };

  const resetSearch = () => {
    setSearchResults(null);
    setSearchQuery('');
    setCurrentPage(0);
    setTotalPages(0);
    setHasNext(false);
    setHasPrevious(false);
    setError(null);
  };

  const NewsCard = ({ newsItem, companyName = '' }) => (
    <div style={{
      backgroundColor: 'white',
      borderRadius: '12px',
      padding: '20px',
      marginBottom: '16px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
      border: '1px solid #f0f0f0',
      transition: 'all 0.2s ease'
    }}
    onMouseEnter={(e) => {
      e.currentTarget.style.transform = 'translateY(-2px)';
      e.currentTarget.style.boxShadow = '0 4px 16px rgba(0,0,0,0.15)';
    }}
    onMouseLeave={(e) => {
      e.currentTarget.style.transform = 'translateY(0)';
      e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
    }}
    >
      {companyName && (
        <div style={{
          fontSize: '12px',
          color: '#3498db',
          fontWeight: '600',
          marginBottom: '8px',
          textTransform: 'uppercase'
        }}>
          {companyName}
        </div>
      )}
      <h3 style={{
        fontSize: '16px',
        fontWeight: '600',
        color: '#2c3e50',
        marginBottom: '8px',
        lineHeight: '1.4',
        cursor: 'pointer'
      }}
      onClick={() => window.open(newsItem.originallink || newsItem.link, '_blank')}
      >
        {newsItem.title || '제목 정보 없음'}
      </h3>
      <p style={{
        fontSize: '14px',
        color: '#6c757d',
        lineHeight: '1.5',
        marginBottom: '12px'
      }}>
        {newsItem.description || '뉴스 내용 미리보기를 불러올 수 없습니다.'}
      </p>
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <span style={{
          fontSize: '12px',
          color: '#adb5bd'
        }}>
          {newsItem.pubDate ? new Date(newsItem.pubDate).toLocaleDateString('ko-KR') : '날짜 정보 없음'}
        </span>
        <button
          onClick={() => window.open(newsItem.originallink || newsItem.link, '_blank')}
          style={{
            backgroundColor: '#3498db',
            color: 'white',
            border: 'none',
            borderRadius: '20px',
            padding: '6px 12px',
            fontSize: '12px',
            cursor: 'pointer',
            transition: 'background-color 0.2s ease'
          }}
          onMouseEnter={(e) => e.target.style.backgroundColor = '#2980b9'}
          onMouseLeave={(e) => e.target.style.backgroundColor = '#3498db'}
        >
          기사 보기
        </button>
      </div>
    </div>
  );

  return (
    <div style={{
      padding: '20px',
      maxWidth: '1200px',
      margin: '0 auto',
      fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
      backgroundColor: '#f8f9fa',
      minHeight: '100vh'
    }}>
      {/* 헤더 */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '12px',
        padding: '30px',
        marginBottom: '30px',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
        textAlign: 'center'
      }}>
        <h1 style={{
          fontSize: '32px',
          color: '#2c3e50',
          fontWeight: '600',
          margin: '0 0 10px 0'
        }}>
          📰 기업 뉴스
        </h1>
        <p style={{
          color: '#6c757d',
          fontSize: '16px',
          margin: '0'
        }}>
          실시간 기업 뉴스를 확인하세요
        </p>
      </div>

      {/* 에러 메시지 */}
      {error && (
        <div style={{
          backgroundColor: '#fff5f5',
          borderRadius: '12px',
          padding: '20px',
          marginBottom: '20px',
          border: '2px solid #fed7d7',
          color: '#c53030'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ fontSize: '20px' }}>⚠️</span>
            <span>{error}</span>
            <button
              onClick={() => setError(null)}
              style={{
                marginLeft: 'auto',
                background: 'none',
                border: 'none',
                fontSize: '18px',
                cursor: 'pointer',
                color: '#c53030'
              }}
            >
              ✖
            </button>
          </div>
        </div>
      )}

      {/* 검색 섹션 */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '12px',
        padding: '20px',
        marginBottom: '30px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
      }}>
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="검색할 기업명을 입력하세요... (예: 삼성, LG, 현대, 네이버, 카카오)"
            style={{
              flex: 1,
              padding: '12px 16px',
              border: '2px solid #e9ecef',
              borderRadius: '25px',
              fontSize: '14px',
              outline: 'none',
              transition: 'border-color 0.2s ease'
            }}
            onFocus={(e) => e.target.style.borderColor = '#3498db'}
            onBlur={(e) => e.target.style.borderColor = '#e9ecef'}
            onKeyPress={(e) => {
              if (e.key === 'Enter') {
                handleSearch(e);
              }
            }}
          />
          <button
            type="submit"
            disabled={loading}
            style={{
              backgroundColor: '#3498db',
              color: 'white',
              border: 'none',
              borderRadius: '25px',
              padding: '12px 24px',
              fontSize: '14px',
              fontWeight: '600',
              cursor: loading ? 'not-allowed' : 'pointer',
              opacity: loading ? 0.7 : 1,
              transition: 'all 0.2s ease'
            }}
          >
            {loading ? '검색 중...' : '🔍 검색'}
          </button>
          {searchResults && (
            <button
              type="button"
              onClick={resetSearch}
              style={{
                backgroundColor: '#95a5a6',
                color: 'white',
                border: 'none',
                borderRadius: '25px',
                padding: '12px 24px',
                fontSize: '14px',
                fontWeight: '600',
                cursor: 'pointer'
              }}
            >
              초기화
            </button>
          )}
        </form>
      </div>

      {/* 검색 결과 */}
      {searchResults && (
        <div style={{ marginBottom: '30px' }}>
          <h2 style={{
            fontSize: '24px',
            color: '#2c3e50',
            marginBottom: '20px',
            fontWeight: '600'
          }}>
            "{searchResults.query}" 검색 결과 ({searchResults.totalResults}건)
            {totalPages > 0 && (
              <span style={{ fontSize: '16px', color: '#6c757d', fontWeight: '400' }}>
                {' '}- {currentPage + 1}페이지 / 총 {totalPages}페이지
              </span>
            )}
          </h2>
          
          {loading ? (
            <div style={{
              backgroundColor: 'white',
              borderRadius: '12px',
              padding: '40px',
              textAlign: 'center',
              color: '#6c757d'
            }}>
              <div style={{ fontSize: '48px', marginBottom: '16px' }}>⏳</div>
              <div>검색 중...</div>
            </div>
          ) : (
            <div>
              {searchResults.news.length > 0 ? (
                <div>
                  {searchResults.news.map((newsItem, index) => (
                    <NewsCard key={index} newsItem={newsItem} />
                  ))}
                  
                  {/* 페이지네이션 */}
                  <Pagination 
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                    hasNext={hasNext}
                    hasPrevious={hasPrevious}
                  />
                </div>
              ) : (
                <div style={{
                  backgroundColor: 'white',
                  borderRadius: '12px',
                  padding: '40px',
                  textAlign: 'center',
                  color: '#6c757d'
                }}>
                  <div style={{ fontSize: '48px', marginBottom: '16px' }}>😔</div>
                  <div>검색 결과가 없습니다.</div>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* 기본 뉴스 섹션 */}
      {!searchResults && (
        <div>
          <h2 style={{
            fontSize: '24px',
            color: '#2c3e50',
            marginBottom: '20px',
            fontWeight: '600'
          }}>
            🏢 주요 기업 뉴스
          </h2>
          {loading ? (
            <div style={{
              backgroundColor: 'white',
              borderRadius: '12px',
              padding: '40px',
              textAlign: 'center',
              color: '#6c757d'
            }}>
              <div style={{ fontSize: '48px', marginBottom: '16px' }}>⏳</div>
              <div>뉴스를 불러오는 중...</div>
            </div>
          ) : (
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))',
              gap: '20px'
            }}>
              {defaultNews.map((companyNews, companyIndex) => (
                <div key={companyIndex} style={{
                  backgroundColor: 'white',
                  borderRadius: '12px',
                  padding: '20px',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
                }}>
                  <h3 style={{
                    fontSize: '18px',
                    color: '#2c3e50',
                    marginBottom: '16px',
                    fontWeight: '600',
                    borderBottom: '2px solid #3498db',
                    paddingBottom: '8px'
                  }}>
                    {companyNews.query}
                  </h3>
                  {companyNews.news.slice(0, 3).map((newsItem, newsIndex) => (
                    <NewsCard
                      key={newsIndex}
                      newsItem={newsItem}
                      companyName={companyNews.query}
                    />
                  ))}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default CompanyNews;