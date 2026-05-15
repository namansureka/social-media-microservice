package com.naman.redis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {
    private Long authorId;
    private String content;
}
