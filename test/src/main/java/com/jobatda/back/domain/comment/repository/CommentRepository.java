package com.jobatda.back.domain.comment.repository;

import com.jobatda.back.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 최상위 댓글들을 생성일시 순으로 조회 (대댓글 포함)
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.children WHERE c.board.id = :boardId AND c.parent IS NULL ORDER BY c.createdAt")
    List<Comment> findTopLevelCommentsByBoardId(@Param("boardId") Long boardId);

    // 특정 게시글의 모든 댓글 수 조회
    Long countByBoardId(Long boardId);
}