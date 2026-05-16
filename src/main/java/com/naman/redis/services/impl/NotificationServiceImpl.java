package com.naman.redis.services.impl;
import com.naman.redis.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int COOLDOWN_MINUTES = 15;

    @Override
    public void processBotInteraction(Long userId, String message) {

        String cooldownKey = "user:" + userId + ":notif_cooldown";
        String pendingKey = "user:" + userId + ":pending_notifs";

        Boolean canSendImmediately = redisTemplate.opsForValue().setIfAbsent(
                cooldownKey,
                "1",
                COOLDOWN_MINUTES,
                TimeUnit.MINUTES
        );

        if(Boolean.TRUE.equals(canSendImmediately)){

            System.out.println("Push Notification Sent to User : " + message);
        }
        else{
            redisTemplate.opsForList().rightPush(pendingKey, message);
        }
    }

    @Override
    public void processPendingNotifications() {

    }
}
