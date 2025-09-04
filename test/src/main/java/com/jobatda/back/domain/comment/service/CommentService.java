package com.jobatda.back.domain.comment.service;

import com.jobatda.back.domain.board.entity.Board;
import com.jobatda.back.domain.board.repository.BoardRepository;
import com.jobatda.back.domain.comment.dto.CommentCreateRequest;
import com.jobatda.back.domain.comment.dto.CommentUpdateRequest;
import com.jobatda.back.domain.comment.entity.Comment;
import com.jobatda.back.domain.comment.repository.CommentRepository;
import com.jobatda.back.domain.user.entity.User;
import com.jobatda.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment createComment(Long boardId, CommentCreateRequest request, Long authorId) {
        // 게시글 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID=" + boardId));

        // 작성자 조회
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID=" + authorId));

        // 부모 댓글 조회 (대댓글인 경우)
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다. ID=" + request.getParentId()));
        }

        // 댓글 엔티티 생성 (작성자 정보 포함)
        Comment comment = Comment.builder()
                .board(board)
                .author(author)
                .content(request.getContent())
                .parent(parent)
                .build();
                
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByBoardId(Long boardId) {
        return commentRepository.findTopLevelCommentsByBoardId(boardId);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID=" + id));
    }

    @Transactional
    public Comment updateComment(Long id, CommentUpdateRequest request) {
        Comment comment = getCommentById(id);
        comment.update(request.getContent());
        return comment;
    }

    @Transactional
    public void deleteComment(Long id) {
        Comment comment = getCommentById(id);
        commentRepository.delete(comment);
    }

    public Long getCommentCount(Long boardId) {
        return commentRepository.countByBoardId(boardId);
    }
}