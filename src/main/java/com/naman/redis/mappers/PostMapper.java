package com.naman.redis.mappers;

import com.naman.redis.dto.PostRequestDto;
import com.naman.redis.dto.PostResponseDto;
import com.naman.redis.entities.Post;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    Post toEntity(PostRequestDto dto);
    PostResponseDto toDto(Post post);
}
