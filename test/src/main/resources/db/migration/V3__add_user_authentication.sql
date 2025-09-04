-- V3__add_user_authentication.sql
-- 사용자 인증 시스템 추가를 위한 데이터베이스 스키마 변경
-- 
-- 주요 변경사항:
-- 1. users 테이블 생성 (사용자 정보 및 권한 관리)
-- 2. boards 테이블에 author_id 외래키 컬럼 추가
-- 3. comments 테이블의 author 컬럼을 author_id 외래키로 변경
-- 4. 기본 사용자 데이터 추가 (테스트용)

-- =====================================================
-- 1. users 테이블 생성
-- =====================================================

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 식별자',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인용 사용자명 (중복불가)',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호 (BCrypt 해시)',
    nickname VARCHAR(20) NOT NULL UNIQUE COMMENT '게시글/댓글에 표시될 닉네임 (중복불가)',
    email VARCHAR(100) UNIQUE COMMENT '사용자 이메일 (선택사항, 중복불가)',
    role ENUM('ADMIN', 'COMPANY', 'GENERAL') NOT NULL DEFAULT 'GENERAL' COMMENT '사용자 권한',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 활성화 상태',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '계정 정보 수정일시',
    
    PRIMARY KEY (id),
    INDEX idx_username (username),      -- 로그인 성능 향상
    INDEX idx_nickname (nickname),      -- 닉네임 검색 성능 향상
    INDEX idx_email (email),           -- 이메일 검색 성능 향상
    INDEX idx_role (role),             -- 권한별 조회 성능 향상
    INDEX idx_enabled (enabled)        -- 활성 사용자 조회 성능 향상
) COMMENT = '사용자 정보 및 인증 관리 테이블';

-- =====================================================
-- 2. 기본 사용자 데이터 생성 (테스트용)
-- =====================================================

-- 관리자 계정 (비밀번호: admin123)
INSERT INTO users (username, password, nickname, email, role, enabled) VALUES 
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye8iyR7oP8s0xDH1YdXOCVxEPVVqBVQ9W', '관리자', 'admin@jobatda.com', 'ADMIN', TRUE);

-- 회사원 계정 (비밀번호: company123)
INSERT INTO users (username, password, nickname, email, role, enabled) VALUES 
('company', '$2a$10$fSAiEFVQL1hP0hYSL8FDtOOJJLlXh8QvL9TXD4nKNshW5qNGxfDeC', '기업담당자', 'company@jobatda.com', 'COMPANY', TRUE);

-- 일반 사용자 계정들 (비밀번호: user123)
INSERT INTO users (username, password, nickname, email, role, enabled) VALUES 
('user1', '$2a$10$tP7O.mfkZPUoL/9VyJlxNOX8vPc8T5Hs9k5d9gLkQ6.8VXJbk.0De', '코딩초보', 'user1@example.com', 'GENERAL', TRUE),
('user2', '$2a$10$tP7O.mfkZPUoL/9VyJlxNOX8vPc8T5Hs9k5d9gLkQ6.8VXJbk.0De', '개발자꿈나무', 'user2@example.com', 'GENERAL', TRUE),
('user3', '$2a$10$tP7O.mfkZPUoL/9VyJlxNOX8vPc8T5Hs9k5d9gLkQ6.8VXJbk.0De', '프론트엔드마스터', 'user3@example.com', 'GENERAL', TRUE);

-- =====================================================
-- 3. boards 테이블에 author_id 컬럼 추가
-- =====================================================

-- 먼저 author_id 컬럼을 NULL 허용으로 추가 (기존 데이터 때문)
ALTER TABLE boards 
ADD COLUMN author_id BIGINT NULL COMMENT '게시글 작성자 ID (users.id 참조)';

-- 외래키 제약조건 추가
ALTER TABLE boards 
ADD CONSTRAINT fk_boards_author 
FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL;

-- 인덱스 추가 (성능 향상)
CREATE INDEX idx_boards_author_id ON boards(author_id);

-- 기존 게시글에 임시 작성자 할당 (관리자로 설정)
UPDATE boards SET author_id = 1 WHERE author_id IS NULL;

-- =====================================================
-- 4. comments 테이블 수정 (author 컬럼을 author_id로 변경)
-- =====================================================

-- 먼저 새로운 author_id 컬럼 추가
ALTER TABLE comments 
ADD COLUMN author_id BIGINT NULL COMMENT '댓글 작성자 ID (users.id 참조)';

-- 기존 댓글에 임시 작성자 할당
-- 기존 author 컬럼의 값에 따라 적절한 사용자 ID 매핑
UPDATE comments SET author_id = 1 WHERE author IS NOT NULL; -- 기존 댓글은 관리자로 설정

-- 외래키 제약조건 추가
ALTER TABLE comments 
ADD CONSTRAINT fk_comments_author 
FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL;

-- 인덱스 추가 (성능 향상)
CREATE INDEX idx_comments_author_id ON comments(author_id);

-- 기존 author 컬럼 삭제 (더 이상 사용하지 않음)
ALTER TABLE comments DROP COLUMN author;

-- author_id를 NOT NULL로 변경 (새로운 댓글은 반드시 작성자가 있어야 함)
ALTER TABLE comments MODIFY COLUMN author_id BIGINT NOT NULL;

-- =====================================================
-- 5. 샘플 게시글 및 댓글 작성자 정보 업데이트
-- =====================================================

-- 공지사항 게시글을 관리자 작성으로 변경
UPDATE boards SET author_id = 1 WHERE board_type = 'NOTICE';

-- 기업게시판 게시글을 기업담당자 작성으로 변경  
UPDATE boards SET author_id = 2 WHERE board_type = 'COMPANY';

-- 자유게시판 게시글들을 다양한 사용자가 작성한 것으로 변경
UPDATE boards SET author_id = 3 WHERE board_type = 'FREE' AND id % 3 = 0;
UPDATE boards SET author_id = 4 WHERE board_type = 'FREE' AND id % 3 = 1;
UPDATE boards SET author_id = 5 WHERE board_type = 'FREE' AND id % 3 = 2;

-- Q&A 게시글들을 일반 사용자들이 작성한 것으로 변경
UPDATE boards SET author_id = 3 WHERE board_type = 'QNA' AND id % 2 = 0;
UPDATE boards SET author_id = 4 WHERE board_type = 'QNA' AND id % 2 = 1;

-- =====================================================
-- 6. 데이터 무결성 검증 쿼리 (주석처리, 필요시 실행)
-- =====================================================

/*
-- 사용자별 게시글 수 확인
SELECT u.nickname, COUNT(b.id) as board_count 
FROM users u 
LEFT JOIN boards b ON u.id = b.author_id 
GROUP BY u.id, u.nickname;

-- 사용자별 댓글 수 확인  
SELECT u.nickname, COUNT(c.id) as comment_count 
FROM users u 
LEFT JOIN comments c ON u.id = c.author_id 
GROUP BY u.id, u.nickname;

-- 권한별 사용자 수 확인
SELECT role, COUNT(*) as user_count 
FROM users 
GROUP BY role;

-- 고아 레코드 확인 (외래키 제약조건 위반)
SELECT 'boards' as table_name, COUNT(*) as orphan_count 
FROM boards 
WHERE author_id NOT IN (SELECT id FROM users)
UNION
SELECT 'comments' as table_name, COUNT(*) as orphan_count 
FROM comments 
WHERE author_id NOT IN (SELECT id FROM users);
*/