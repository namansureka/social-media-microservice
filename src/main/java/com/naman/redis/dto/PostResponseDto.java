package com.naman.redis.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostResponseDto {
    private Long id;
    private Long authorId;
    private String content;
    private LocalDateTime createdAt;
}
