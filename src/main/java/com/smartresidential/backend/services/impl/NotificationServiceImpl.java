package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.notification.CreateNotificationRequest;
import com.smartresidential.backend.dto.notification.NotificationResponseDTO;
import com.smartresidential.backend.entities.Notification;
import com.smartresidential.backend.repositories.NotificationRepository;
import com.smartresidential.backend.services.interfaces.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> notifications =
                repository.findByUserIdAndIsReadFalse(userId);

        notifications.forEach(n -> n.setIsRead(true));
        repository.saveAll(notifications);
    }

    @Override
    public NotificationResponseDTO create(CreateNotificationRequest request) {
        return null;
    }

    @Override
    public List<NotificationResponseDTO> getByUser(Long userId) {
        return null;
    }

    @Override
    public List<NotificationResponseDTO> getUnread(Long userId) {
        return null;
    }

    @Override
    public void markAsRead(Long id) {
    }
}