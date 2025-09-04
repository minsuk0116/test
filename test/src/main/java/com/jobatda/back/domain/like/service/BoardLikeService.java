package com.jobatda.back.domain.like.service;

import com.jobatda.back.domain.board.entity.Board;
import com.jobatda.back.domain.board.repository.BoardRepository;
import com.jobatda.back.domain.like.dto.LikeResponse;
import com.jobatda.back.domain.like.entity.BoardLike;
import com.jobatda.back.domain.like.repository.BoardLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardLikeService {

    private final BoardLikeRepository boardLikeRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public LikeResponse toggleLike(Long boardId, String userIdentifier) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID=" + boardId));

        Optional<BoardLike> existingLike = boardLikeRepository.findByBoardIdAndUserIdentifier(boardId, userIdentifier);

        if (existingLike.isPresent()) {
            // 좋아요가 이미 있으면 제거
            boardLikeRepository.delete(existingLike.get());
            Long likeCount = boardLikeRepository.countByBoardId(boardId);
            return LikeResponse.of(false, likeCount);
        } else {
            // 좋아요가 없으면 추가
            BoardLike boardLike = BoardLike.builder()
                    .board(board)
                    .userIdentifier(userIdentifier)
                    .build();
            boardLikeRepository.save(boardLike);
            Long likeCount = boardLikeRepository.countByBoardId(boardId);
            return LikeResponse.of(true, likeCount);
        }
    }

    public LikeResponse getLikeStatus(Long boardId, String userIdentifier) {
        boolean liked = boardLikeRepository.existsByBoardIdAndUserIdentifier(boardId, userIdentifier);
        Long likeCount = boardLikeRepository.countByBoardId(boardId);
        return LikeResponse.of(liked, likeCount);
    }

    public Long getLikeCount(Long boardId) {
        return boardLikeRepository.countByBoardId(boardId);
    }
}