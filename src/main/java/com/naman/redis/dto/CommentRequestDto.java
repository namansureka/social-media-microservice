package com.naman.redis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {
    private Long postId;
    private Long authorId;
    private String content;
    private int depthLevel;
}
