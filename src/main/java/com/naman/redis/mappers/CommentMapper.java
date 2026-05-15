package com.naman.redis.mappers;

import com.naman.redis.dto.CommentRequestDto;
import com.naman.redis.dto.CommentResponseDto;
import com.naman.redis.entities.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface CommentMapper {
    Comment toEntity(CommentRequestDto commentRequestDto);
    CommentResponseDto toDto(Comment comment);
}
