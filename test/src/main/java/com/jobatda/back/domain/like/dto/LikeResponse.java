package com.jobatda.back.domain.like.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeResponse {

    private boolean liked;
    private Long likeCount;

    public static LikeResponse of(boolean liked, Long likeCount) {
        return LikeResponse.builder()
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }
}