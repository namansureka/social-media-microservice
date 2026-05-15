package com.naman.redis.services.impl;

import com.naman.redis.services.GuardrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GuardrailServiceImpl implements GuardrailService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void incrementViralityScore(Long postId, int points) {
        redisTemplate.opsForValue().increment("post:" + postId + ":virality_score", points);
    }

    @Override
    public Long incrementBotCount(Long postId) {
        return redisTemplate.opsForValue().increment("post:" + postId + ":bot_count");
    }

    @Override
    public void checkBotCommentAllowed(Long postId, Long botId, Long humanId, int depthLevel) {

        if (depthLevel > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thread too deep");
        }

        Long count = incrementBotCount(postId);
        if (count > 100) {
            redisTemplate.opsForValue().decrement("post:" + postId + ":bot_count");
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Bot reply limit reached");
        }

        Boolean cooldownSet = redisTemplate.opsForValue().setIfAbsent(
                "cooldown:bot_" + botId + ":human_" + humanId,
                "1",
                10,
                TimeUnit.MINUTES
        );

        if (Boolean.FALSE.equals(cooldownSet)) {
            redisTemplate.opsForValue().decrement("post:" + postId + ":bot_count");
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Cooldown active");
        }
    }
}