package com.smartresidential.backend.jobs;

import com.smartresidential.backend.entities.Notification;
import com.smartresidential.backend.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupJob {

    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteOldReadNotifications() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(30);

        List<Notification> oldNotifications =
                notificationRepository.findByIsReadTrueAndCreatedAtBefore(limitDate);

        notificationRepository.deleteAll(oldNotifications);

        log.info("Background job completed: deleted {} old read notifications", oldNotifications.size());
    }
}
