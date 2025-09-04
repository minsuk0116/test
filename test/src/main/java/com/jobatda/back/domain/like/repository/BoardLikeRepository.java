package com.jobatda.back.domain.like.repository;

import com.jobatda.back.domain.like.entity.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

    // 특정 게시글에 대한 사용자의 좋아요 조회
    Optional<BoardLike> findByBoardIdAndUserIdentifier(Long boardId, String userIdentifier);

    // 특정 게시글의 좋아요 수 조회
    Long countByBoardId(Long boardId);

    // 특정 게시글에 대한 사용자의 좋아요 여부 확인
    boolean existsByBoardIdAndUserIdentifier(Long boardId, String userIdentifier);

    // 특정 게시글에 대한 사용자의 좋아요 삭제
    void deleteByBoardIdAndUserIdentifier(Long boardId, String userIdentifier);
}