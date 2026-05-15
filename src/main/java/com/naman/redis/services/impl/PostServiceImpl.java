package com.naman.redis.services.impl;


import com.naman.redis.dto.CommentRequestDto;
import com.naman.redis.dto.CommentResponseDto;
import com.naman.redis.dto.PostRequestDto;
import com.naman.redis.dto.PostResponseDto;
import com.naman.redis.entities.Comment;
import com.naman.redis.entities.Post;
import com.naman.redis.mappers.CommentMapper;
import com.naman.redis.mappers.PostMapper;
import com.naman.redis.repositories.CommentRepository;
import com.naman.redis.repositories.PostRepository;
import com.naman.redis.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Override
    public PostResponseDto createPost(PostRequestDto request) {
        Post post = postMapper.toEntity(request);
        post.setCreatedAt(LocalDateTime.now());
        post= postRepository.save(post);
        return postMapper.toDto(post);

    }

    @Override
    public CommentResponseDto addComment(Long postId, CommentRequestDto request) {
        Comment comment = commentMapper.toEntity(request);
        comment.setPostId(postId);
        comment.setCreatedAt(LocalDateTime.now());

        if (request.getParentCommentId() == null) {
            comment.setDepthLevel(1);
        } else {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setDepthLevel(parent.getDepthLevel() + 1);
        }

        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    @Override
    public void likePost(Long postId, Long userId) {
        //TODO

    }
}
