package com.jobatda.back.domain.like.controller;

import com.jobatda.back.domain.like.dto.LikeRequest;
import com.jobatda.back.domain.like.dto.LikeResponse;
import com.jobatda.back.domain.like.service.BoardLikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boards/{boardId}/likes")
@RequiredArgsConstructor
@Slf4j
public class BoardLikeController {

    private final BoardLikeService boardLikeService;

    @PostMapping("/toggle")
    public LikeResponse toggleLike(@PathVariable Long boardId,
                                 @Valid @RequestBody LikeRequest request) {
        log.info("좋아요 토글 요청 - boardId: {}, userIdentifier: {}", boardId, request.getUserIdentifier());
        return boardLikeService.toggleLike(boardId, request.getUserIdentifier());
    }

    @GetMapping
    public LikeResponse getLikeStatus(@PathVariable Long boardId,
                                    @RequestParam String userIdentifier) {
        return boardLikeService.getLikeStatus(boardId, userIdentifier);
    }

    @GetMapping("/count")
    public Long getLikeCount(@PathVariable Long boardId) {
        return boardLikeService.getLikeCount(boardId);
    }
}