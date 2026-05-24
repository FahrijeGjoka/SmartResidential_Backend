package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.notification.CreateNotificationRequest;
import com.smartresidential.backend.dto.notification.NotificationFilterRequest;
import com.smartresidential.backend.dto.notification.NotificationResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {

    NotificationResponseDTO create(CreateNotificationRequest request);

    List<NotificationResponseDTO> getByUser(Long userId);

    List<NotificationResponseDTO> getUnread(Long userId);

    Page<NotificationResponseDTO> search(NotificationFilterRequest filter);

    void markAsRead(Long id);

    void markAllAsRead(Long userId);
}
