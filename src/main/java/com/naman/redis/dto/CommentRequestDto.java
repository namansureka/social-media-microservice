package com.naman.redis.dto;

import com.naman.redis.entities.AuthorType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {
    private Long authorId;
    private String content;
    private Long parentCommentId;
    @NotNull(message = "authorType is required")
    private AuthorType authorType;
}
