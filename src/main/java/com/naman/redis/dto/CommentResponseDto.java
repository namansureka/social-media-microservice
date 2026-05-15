package com.naman.redis.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResponseDto {
    private Long id;
    private Long postId;
    private Long authorId;
    private String content;
    private int depthLevel;
    private LocalDateTime createdAt;
}
