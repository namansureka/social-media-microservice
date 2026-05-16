package com.naman.redis.services.impl;
import com.naman.redis.dto.CommentRequestDto;
import com.naman.redis.dto.CommentResponseDto;
import com.naman.redis.dto.PostRequestDto;
import com.naman.redis.dto.PostResponseDto;
import com.naman.redis.entities.AuthorType;
import com.naman.redis.entities.Bot;
import com.naman.redis.entities.Comment;
import com.naman.redis.entities.Post;
import com.naman.redis.mappers.CommentMapper;
import com.naman.redis.mappers.PostMapper;
import com.naman.redis.repositories.BotRepository;
import com.naman.redis.repositories.CommentRepository;
import com.naman.redis.repositories.PostRepository;
import com.naman.redis.services.GuardrailService;
import com.naman.redis.services.NotificationService;
import com.naman.redis.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final GuardrailService guardrailService;
    private final NotificationService notificationService;
    private final BotRepository botRepository;

    @Override
    public PostResponseDto createPost(PostRequestDto request) {
        Post post = postMapper.toEntity(request);
        post.setCreatedAt(LocalDateTime.now());
        post= postRepository.save(post);
        return postMapper.toDto(post);

    }

    @Transactional
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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if(request.getAuthorType()==AuthorType.BOT){
            guardrailService.checkBotCommentAllowed(
                    postId,
                    request.getAuthorId(),
                    post.getAuthorId(),
                    comment.getDepthLevel()
            );
            guardrailService.incrementViralityScore(postId, 1); // bot reply = +1
        }
        else {
            guardrailService.incrementViralityScore(postId, 50);
        }

        String notificationMessage = null;

        if(request.getAuthorType() == AuthorType.BOT){

            Long botId = request.getAuthorId();
            Bot bot = botRepository.findById(botId).orElseThrow(() -> new RuntimeException("Bot not found"));
            notificationMessage = bot.getName() + " replied to your post";
        }

        try {
            comment = commentRepository.save(comment);

        } catch (Exception e) {

            if(request.getAuthorType() == AuthorType.BOT){

                guardrailService.rollbackBotCount(postId);
                guardrailService.rollbackCooldown(
                        request.getAuthorId(),
                        post.getAuthorId()
                );
                guardrailService.rollbackViralityScore(postId,1);
            }
            else{

                guardrailService.rollbackViralityScore(postId,50);
            }
            throw e;
        }
        if(notificationMessage != null){

            notificationService.processBotInteraction(
                    post.getAuthorId(),
                    notificationMessage
            );
        }
        return commentMapper.toDto(comment);
    }

    @Override
    public void likePost(Long postId, Long userId) {
        guardrailService.incrementViralityScore(postId, 20);
    }
}
