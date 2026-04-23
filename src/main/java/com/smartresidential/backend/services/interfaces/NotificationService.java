package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.notification.CreateNotificationRequest;
import com.smartresidential.backend.dto.notification.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {

    NotificationResponseDTO create(CreateNotificationRequest request);

    List<NotificationResponseDTO> getByUser(Long userId);

    List<NotificationResponseDTO> getUnread(Long userId);

    void markAsRead(Long id);

    void markAllAsRead(Long userId);
}