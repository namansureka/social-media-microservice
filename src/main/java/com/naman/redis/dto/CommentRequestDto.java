package com.naman.redis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {
    private Long authorId;
    private String content;
    private Long parentCommentId;
}
