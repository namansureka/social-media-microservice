package com.naman.redis.controllers;

import com.naman.redis.dto.CommentRequestDto;
import com.naman.redis.dto.CommentResponseDto;
import com.naman.redis.dto.PostRequestDto;
import com.naman.redis.dto.PostResponseDto;
import com.naman.redis.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/*
POST /api/posts - Create a new post.
POST /api/posts/{postId}/comments - Add a comment to a post.
POST /api/posts/{postId}/like - Like a post.
*/


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {


    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto request) {
        PostResponseDto response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponseDto> addComment(@PathVariable Long postId,@RequestBody CommentRequestDto request) {
        CommentResponseDto commentResponseDto=postService.addComment(postId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponseDto);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId,@RequestParam(required = true) Long userId) {
        postService.likePost(postId,userId);
        return ResponseEntity.ok().build();
    }
}
