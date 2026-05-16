package com.naman.redis.services;

import org.springframework.stereotype.Service;

@Service
public interface NotificationService {

    void processBotInteraction(Long userId, String message);
    void processPendingNotifications();
}
