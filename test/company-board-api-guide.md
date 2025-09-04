# 기업게시판 API 사용 가이드

## 📋 개요
기업게시판 기능이 추가되었습니다! 이제 **공지사항 → 기업게시판 → 자유게시판 → Q&A** 순서로 4개의 게시판을 사용할 수 있습니다.

## 🚀 새로 추가된 API 엔드포인트

### 1. 게시판별 통계 조회 (업데이트됨)
```
GET /boards/stats
```
**응답 예시:**
```json
{
  "boardCounts": {
    "NOTICE": 3,
    "COMPANY": 5,
    "FREE": 10,
    "QNA": 7,
    "TOTAL": 25
  },
  "success": true
}
```

### 2. 특정 게시판 타입 조회 (신규)
```
GET /boards/type/{boardType}
```
**예시:**
- `GET /boards/type/COMPANY` - 기업게시판 전체 조회
- `GET /boards/type/NOTICE` - 공지사항 전체 조회
- `GET /boards/type/FREE` - 자유게시판 전체 조회
- `GET /boards/type/QNA` - Q&A 전체 조회

### 3. 특정 게시판 타입 페이징 조회 (신규)
```
GET /boards/type/{boardType}/page?page=0&size=10
```
**예시:**
- `GET /boards/type/COMPANY/page?page=0&size=5` - 기업게시판 첫 페이지 5개

## 💻 프론트엔드 사용 예시

### React/JavaScript 코드 예시

```javascript
// 1. 기업게시판 목록 조회
const fetchCompanyBoards = async (page = 0, size = 10) => {
  try {
    const response = await fetch(`/boards/type/COMPANY/page?page=${page}&size=${size}`);
    const data = await response.json();
    
    console.log('기업게시판 데이터:', data);
    console.log('총 게시글 수:', data.totalElements);
    console.log('현재 페이지:', data.currentPage);
    console.log('게시글 목록:', data.boards);
    
    return data;
  } catch (error) {
    console.error('기업게시판 조회 실패:', error);
  }
};

// 2. 게시판별 통계 조회 (메뉴에 게시글 수 표시용)
const fetchBoardStats = async () => {
  try {
    const response = await fetch('/boards/stats');
    const data = await response.json();
    
    const stats = data.boardCounts;
    console.log(`공지사항: ${stats.NOTICE}개`);
    console.log(`기업게시판: ${stats.COMPANY}개`);
    console.log(`자유게시판: ${stats.FREE}개`);
    console.log(`Q&A: ${stats.QNA}개`);
    
    return stats;
  } catch (error) {
    console.error('통계 조회 실패:', error);
  }
};

// 3. 기업게시판 게시글 작성
const createCompanyBoard = async (boardData) => {
  try {
    const response = await fetch('/boards', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        title: boardData.title,
        content: boardData.content,
        boardType: 'COMPANY',  // 기업게시판으로 지정
        imageUrl: boardData.imageUrl || null
      })
    });
    
    const newBoard = await response.json();
    console.log('기업게시판 게시글 생성 완료:', newBoard);
    return newBoard;
  } catch (error) {
    console.error('게시글 작성 실패:', error);
  }
};

// 4. 사용 예시
const init = async () => {
  // 통계 조회
  const stats = await fetchBoardStats();
  
  // 기업게시판 첫 페이지 조회
  const companyBoards = await fetchCompanyBoards(0, 10);
  
  // 기업게시판 게시글 작성
  await createCompanyBoard({
    title: '카카오 2025 개발자 채용',
    content: '카카오에서 백엔드 개발자를 모집합니다...',
    imageUrl: 'https://example.com/kakao-logo.png'
  });
};
```

### Vue.js 코드 예시

```vue
<template>
  <div>
    <!-- 게시판 메뉴 -->
    <nav>
      <button @click="selectBoard('NOTICE')">공지사항 ({{ stats.NOTICE }})</button>
      <button @click="selectBoard('COMPANY')">기업게시판 ({{ stats.COMPANY }})</button>
      <button @click="selectBoard('FREE')">자유게시판 ({{ stats.FREE }})</button>
      <button @click="selectBoard('QNA')">Q&A ({{ stats.QNA }})</button>
    </nav>
    
    <!-- 게시글 목록 -->
    <div v-if="selectedBoard === 'COMPANY'">
      <h2>기업게시판</h2>
      <div v-for="board in boards" :key="board.id" class="board-item">
        <h3>{{ board.title }}</h3>
        <p>{{ board.content.substring(0, 100) }}...</p>
        <small>좋아요: {{ board.likeCount }} | 댓글: {{ board.commentCount }}</small>
      </div>
      
      <!-- 페이징 -->
      <div class="pagination">
        <button @click="changePage(currentPage - 1)" :disabled="!hasPrevious">이전</button>
        <span>{{ currentPage + 1 }} / {{ totalPages }}</span>
        <button @click="changePage(currentPage + 1)" :disabled="!hasNext">다음</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      selectedBoard: 'COMPANY',
      boards: [],
      stats: {
        NOTICE: 0,
        COMPANY: 0,
        FREE: 0,
        QNA: 0
      },
      currentPage: 0,
      totalPages: 0,
      hasNext: false,
      hasPrevious: false
    }
  },
  
  async mounted() {
    await this.loadStats();
    await this.loadBoards();
  },
  
  methods: {
    async loadStats() {
      try {
        const response = await fetch('/boards/stats');
        const data = await response.json();
        this.stats = data.boardCounts;
      } catch (error) {
        console.error('통계 로드 실패:', error);
      }
    },
    
    async loadBoards(page = 0) {
      try {
        const response = await fetch(`/boards/type/${this.selectedBoard}/page?page=${page}&size=10`);
        const data = await response.json();
        
        this.boards = data.boards;
        this.currentPage = data.currentPage;
        this.totalPages = data.totalPages;
        this.hasNext = data.hasNext;
        this.hasPrevious = data.hasPrevious;
      } catch (error) {
        console.error('게시글 로드 실패:', error);
      }
    },
    
    async selectBoard(boardType) {
      this.selectedBoard = boardType;
      await this.loadBoards(0);
    },
    
    async changePage(page) {
      if (page >= 0 && page < this.totalPages) {
        await this.loadBoards(page);
      }
    }
  }
}
</script>
```

## 📊 데이터베이스 업데이트

데이터베이스에 기업게시판 샘플 데이터가 추가되었습니다:

1. **삼성전자 신입사원 모집** - 채용 공고
2. **LG전자 AI/빅데이터 전문인력 채용** - 기술직 채용
3. **네이버 웹툰 콘텐츠 기획자 모집** - 기획직 채용  
4. **스타트업 투자 설명회** - 투자/창업 정보
5. **IT 개발자 컨퍼런스 2025** - 행사 안내

## 🎯 주요 기능

### ✅ 완료된 기능
- [x] BoardType enum에 COMPANY 추가
- [x] 데이터베이스 마이그레이션 스크립트 작성
- [x] 기업게시판 샘플 데이터 5개 추가
- [x] 게시판별 통계 API 업데이트 (COMPANY 포함)
- [x] 특정 게시판 타입 조회 API 추가
- [x] 특정 게시판 타입 페이징 조회 API 추가
- [x] BoardService, BoardRepository 메서드 추가

### 📱 프론트엔드 구현 권장사항
1. **메뉴 구성**: 공지사항 → 기업게시판 → 자유게시판 → Q&A 순서
2. **게시글 수 표시**: 각 메뉴에 해당 게시판의 게시글 수 표시
3. **기업게시판 특화**: 채용공고, 기업소식, 투자정보 등에 특화된 UI 고려
4. **필터링**: 기업게시판 내에서 채용/소식/투자 등으로 세분화 가능

## 🔧 테스트 방법

### Postman 또는 curl로 테스트

```bash
# 1. 게시판 통계 조회
curl -X GET http://localhost:8080/boards/stats

# 2. 기업게시판 전체 조회
curl -X GET http://localhost:8080/boards/type/COMPANY

# 3. 기업게시판 페이징 조회 (첫 페이지, 3개)
curl -X GET "http://localhost:8080/boards/type/COMPANY/page?page=0&size=3"

# 4. 기업게시판 게시글 작성
curl -X POST http://localhost:8080/boards \
  -H "Content-Type: application/json" \
  -d '{
    "title": "구글 코리아 채용공고",
    "content": "구글 코리아에서 소프트웨어 엔지니어를 모집합니다.",
    "boardType": "COMPANY"
  }'
```

## 🎨 UI/UX 제안

### 기업게시판 전용 디자인 요소
- 📢 **아이콘**: 기업/비즈니스 관련 아이콘 사용
- 🏢 **색상**: 전문적이고 신뢰감 있는 색상 (예: 파란색 계열)
- 📋 **카드 레이아웃**: 채용공고는 카드 형태로 표시
- 🔍 **태그 시스템**: #채용 #투자 #컨퍼런스 등의 태그 추가

이제 기업게시판이 완전히 준비되었습니다! 🎉