package com.naman.redis.services.impl;
import com.naman.redis.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final StringRedisTemplate redisTemplate;
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

        if (Boolean.TRUE.equals(canSendImmediately)) {

            System.out.println("Push Notification Sent to User : " + message);
        } else {
            redisTemplate.opsForList().rightPush(pendingKey, message);
            redisTemplate.opsForSet().add("pending_users", userId.toString());
        }
    }

    @Override
    public void processPendingNotifications() {

        Set<String> users = redisTemplate.opsForSet().members("pending_users");
        if (users == null|| users.isEmpty()) return;

        for(String user : users) {

            Long userId = Long.valueOf(user);
            String pendingKey = "user:" + userId + ":pending_notifs";
            String processingKey = "user:" + userId + ":processing_notifs";

            if(Boolean.FALSE.equals(redisTemplate.hasKey(pendingKey))){

                redisTemplate.opsForSet().remove("pending_users", userId.toString());
                continue;
            }

            redisTemplate.rename(pendingKey, processingKey);

            List<String> notifications = redisTemplate.opsForList().range(processingKey, 0, -1);

            if(notifications == null || notifications.isEmpty()){

                redisTemplate.delete(processingKey);
                continue;
            }
            String firstNotification = notifications.get(0);

            int othersCount = notifications.size() - 1;
            if(othersCount == 0){
                System.out.println("Summarized Push Notification : " + firstNotification);

            }
            else{

                System.out.println("Summarized Push Notification : " + firstNotification + " and "
                        + othersCount + " others interacted with your posts"
                );
            }
            redisTemplate.delete(processingKey);
            redisTemplate.opsForSet().remove("pending_users", userId.toString());
        }
    }
}
