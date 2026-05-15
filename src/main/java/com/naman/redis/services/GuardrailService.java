package com.naman.redis.services;

import org.springframework.stereotype.Service;

@Service
public interface GuardrailService {

        void incrementViralityScore(Long postId, int points);
        Long incrementBotCount(Long postId);
        void checkBotCommentAllowed(Long postId, Long botId, Long humanId, int depthLevel);

}
