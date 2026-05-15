package com.naman.redis.services;

import com.naman.redis.dto.CommentRequestDto;
import com.naman.redis.dto.CommentResponseDto;
import com.naman.redis.dto.PostRequestDto;
import com.naman.redis.dto.PostResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface PostService {
        PostResponseDto createPost(PostRequestDto request);
        CommentResponseDto addComment(Long postId, CommentRequestDto request);
        void likePost(Long postId, Long userId);
}
