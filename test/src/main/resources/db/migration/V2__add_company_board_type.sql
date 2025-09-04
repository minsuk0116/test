-- =============================================
-- 기업게시판 추가를 위한 데이터베이스 마이그레이션
-- Version: V2__add_company_board_type.sql
-- 작성일: 2025년
-- 설명: BoardType enum에 COMPANY 타입을 추가하고 관련 데이터 업데이트
-- =============================================

-- 1. 기존 boards 테이블의 board_type 컬럼 제약조건 확인
-- (MySQL에서는 ENUM 타입을 사용하는 경우가 많지만, JPA에서는 VARCHAR로 저장됨)

-- 2. 기존 테이블에 COMPANY 타입 게시글이 있는지 확인
SELECT COUNT(*) as existing_company_boards 
FROM boards 
WHERE board_type = 'COMPANY';

-- 3. 샘플 기업게시판 게시글 추가 (테스트용)
INSERT INTO boards (board_type, title, content, image_url) VALUES 
('COMPANY', '삼성전자 2025년 상반기 신입사원 모집', 
 '삼성전자에서 2025년 상반기 신입사원을 모집합니다.
 
모집분야:
- SW개발: Java, Spring, React 개발자
- 하드웨어: 반도체, 디스플레이 엔지니어
- 영업/마케팅: 국내외 영업, 디지털마케팅

지원자격:
- 4년제 대학 졸업(예정)자
- 관련 경력 우대
- 영어 가능자 우대

접수기간: 2025.03.01 ~ 2025.03.31
문의: hr@samsung.com', 
 'https://picsum.photos/800/600?random=samsung'),

('COMPANY', 'LG전자 AI/빅데이터 전문인력 채용', 
 'LG전자에서 AI 및 빅데이터 분야의 전문인력을 채용합니다.

담당업무:
- 머신러닝/딥러닝 모델 개발
- 빅데이터 플랫폼 구축 및 운영
- AI 서비스 기획 및 개발

우대사항:
- Python, TensorFlow, PyTorch 경험
- AWS, GCP 클라우드 경험
- 데이터 분석 프로젝트 경험

연봉: 경력에 따라 협의
근무지: 서울 강남구 LG사이언스파크', 
 'https://picsum.photos/800/600?random=lg'),

('COMPANY', '네이버 웹툰 콘텐츠 기획자 모집', 
 '네이버 웹툰에서 글로벌 콘텐츠 기획자를 모집합니다.

주요업무:
- 웹툰 콘텐츠 기획 및 관리
- 작가 발굴 및 육성
- 해외 진출 전략 수립

자격요건:
- 콘텐츠 기획 경험 3년 이상
- 영어, 일본어 또는 중국어 가능자
- 웹툰/만화 산업에 대한 이해

혜택:
- 4대보험, 퇴직금
- 유연근무제
- 교육비 지원', 
 'https://picsum.photos/800/600?random=naver'),

('COMPANY', '스타트업 투자 설명회 - 핀테크 기업 모집', 
 '2025년 핀테크 스타트업 투자 설명회를 개최합니다.

일시: 2025년 4월 15일 (화) 14:00~18:00
장소: 강남구 테헤란로 스타트업 허브

투자 대상:
- 핀테크, 블록체인 관련 스타트업
- Pre-A ~ Series B 단계 기업
- 월 매출 1억원 이상 또는 높은 성장성

투자 규모: 10억~100억원
참가 신청: startup@invest.com

주최: 한국핀테크협회
후원: 산업은행, 기업은행', 
 'https://picsum.photos/800/600?random=startup'),

('COMPANY', 'IT 개발자 컨퍼런스 2025 개최 안내', 
 '국내 최대 규모의 IT 개발자 컨퍼런스가 개최됩니다.

행사명: DevCon Korea 2025
일시: 2025년 5월 20일~21일
장소: 코엑스 컨벤션센터

주요 세션:
- 클라우드 네이티브 아키텍처
- AI/ML 실무 적용 사례
- 마이크로서비스 설계 패턴
- 블록체인과 웹3.0

연사진:
- 구글, 아마존, 네이버, 카카오 개발자
- 국내외 유명 오픈소스 컨트리뷰터

참가비: 조기등록 15만원, 일반등록 20만원
등록: www.devcon.kr', 
 'https://picsum.photos/800/600?random=conference');

-- 4. 통계 확인 쿼리
SELECT 
    board_type,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM boards), 2) as percentage
FROM boards 
GROUP BY board_type
ORDER BY count DESC;

-- 5. 최근 추가된 기업게시판 게시글 확인
SELECT 
    id,
    board_type,
    title,
    SUBSTRING(content, 1, 100) as content_preview,
    image_url
FROM boards 
WHERE board_type = 'COMPANY'
ORDER BY id DESC
LIMIT 10;

COMMIT;