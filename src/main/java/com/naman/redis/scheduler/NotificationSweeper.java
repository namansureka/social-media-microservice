package com.naman.redis.scheduler;

import com.naman.redis.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationSweeper {

    private final NotificationService notificationService;

    @Scheduled(fixedRate = 5*60*1000)
    public void sweepNotifications() {
        log.info("Starting notification sweep...");
        try {
            notificationService.processPendingNotifications();
            log.info("Notification sweep completed successfully.");
        } catch (Exception e) {
            log.error("Error during notification sweep: ", e);
        }
    }
}
