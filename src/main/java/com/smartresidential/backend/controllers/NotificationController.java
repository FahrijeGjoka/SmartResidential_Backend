package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.notification.CreateNotificationRequest;
import com.smartresidential.backend.dto.notification.NotificationResponseDTO;
import com.smartresidential.backend.services.interfaces.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public NotificationResponseDTO create(@RequestBody CreateNotificationRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<NotificationResponseDTO> getAll() {
        return service.getByUser(null);
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponseDTO> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<NotificationResponseDTO> getUnread(@PathVariable Long userId) {
        return service.getUnread(userId);
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        service.markAsRead(id);
    }

    @PatchMapping("/user/{userId}/read-all")
    public void markAllAsRead(@PathVariable Long userId) {
        service.markAllAsRead(userId);
    }
}