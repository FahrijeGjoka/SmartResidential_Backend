package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.notification.CreateNotificationRequest;
import com.smartresidential.backend.dto.notification.NotificationResponseDTO;
import com.smartresidential.backend.entities.Notification;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.NotificationRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public NotificationResponseDTO create(CreateNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setIsRead(false);

        Notification saved = repository.save(notification);

        return mapToDto(saved);
    }

    @Override
    public List<NotificationResponseDTO> getByUser(Long userId) {
        if (userId == null) {
            return repository.findAll()
                    .stream()
                    .map(this::mapToDto)
                    .toList();
        }

        return repository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<NotificationResponseDTO> getUnread(Long userId) {
        return repository.findByUserIdAndIsReadFalse(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        notification.setIsRead(true);
        repository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = repository.findByUserIdAndIsReadFalse(userId);

        notifications.forEach(n -> n.setIsRead(true));
        repository.saveAll(notifications);
    }

    private NotificationResponseDTO mapToDto(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();

        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setIsRead(notification.getIsRead());

        if (notification.getCreatedAt() != null) {
            dto.setCreatedAt(notification.getCreatedAt().toString());
        }

        return dto;
    }
}