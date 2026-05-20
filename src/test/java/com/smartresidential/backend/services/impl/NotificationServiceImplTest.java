package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.notification.CreateNotificationRequest;
import com.smartresidential.backend.dto.notification.NotificationResponseDTO;
import com.smartresidential.backend.entities.Notification;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.NotificationRepository;
import com.smartresidential.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private UserRepository userRepository;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(repository, userRepository);
    }

    @Test
    void createCreatesNotificationSuccessfully() {

        CreateNotificationRequest request =
                new CreateNotificationRequest();

        request.setUserId(1L);
        request.setMessage("Issue assigned");
        request.setType("ISSUE");

        User user = user(1L);

        Notification savedNotification = notification(
                10L,
                user,
                "Issue assigned",
                "ISSUE",
                false
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(repository.save(any(Notification.class)))
                .thenReturn(savedNotification);

        NotificationResponseDTO response =
                service.create(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("Issue assigned");
        assertThat(response.getType()).isEqualTo("ISSUE");
        assertThat(response.getIsRead()).isFalse();

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(repository).save(captor.capture());

        Notification captured = captor.getValue();

        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.getMessage()).isEqualTo("Issue assigned");
        assertThat(captured.getType()).isEqualTo("ISSUE");
        assertThat(captured.getIsRead()).isFalse();
    }

    @Test
    void createThrowsExceptionWhenUserDoesNotExist() {

        CreateNotificationRequest request =
                new CreateNotificationRequest();

        request.setUserId(99L);
        request.setMessage("Message");
        request.setType("SYSTEM");

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 99");

        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void getByUserReturnsMappedNotifications() {

        User user = user(1L);

        Notification notification = notification(
                10L,
                user,
                "New notification",
                "SYSTEM",
                false
        );

        when(repository.findByUserId(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponseDTO> result =
                service.getByUser(1L);

        assertThat(result).hasSize(1);

        NotificationResponseDTO dto = result.get(0);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getMessage()).isEqualTo("New notification");
        assertThat(dto.getType()).isEqualTo("SYSTEM");
        assertThat(dto.getIsRead()).isFalse();
    }

    @Test
    void getByUserWithNullReturnsAllNotifications() {

        User user = user(1L);

        Notification notification = notification(
                20L,
                user,
                "All notifications",
                "GENERAL",
                false
        );

        when(repository.findAll())
                .thenReturn(List.of(notification));

        List<NotificationResponseDTO> result =
                service.getByUser(null);

        assertThat(result).hasSize(1);

        verify(repository).findAll();
        verify(repository, never()).findByUserId(any());
    }

    @Test
    void getUnreadReturnsOnlyUnreadNotifications() {

        User user = user(1L);

        Notification notification = notification(
                30L,
                user,
                "Unread message",
                "ISSUE",
                false
        );

        when(repository.findByUserIdAndIsReadFalse(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponseDTO> result =
                service.getUnread(1L);

        assertThat(result).hasSize(1);

        NotificationResponseDTO dto = result.get(0);

        assertThat(dto.getIsRead()).isFalse();
        assertThat(dto.getMessage()).isEqualTo("Unread message");
    }

    @Test
    void markAsReadUpdatesNotificationSuccessfully() {

        User user = user(1L);

        Notification notification = notification(
                40L,
                user,
                "Pending notification",
                "SYSTEM",
                false
        );

        when(repository.findById(40L))
                .thenReturn(Optional.of(notification));

        service.markAsRead(40L);

        assertThat(notification.getIsRead()).isTrue();

        verify(repository).save(notification);
    }

    @Test
    void markAsReadThrowsExceptionWhenNotificationDoesNotExist() {

        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification not found with id: 99");

        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void markAllAsReadUpdatesAllUnreadNotifications() {

        User user = user(1L);

        Notification first = notification(
                1L,
                user,
                "First",
                "SYSTEM",
                false
        );

        Notification second = notification(
                2L,
                user,
                "Second",
                "ISSUE",
                false
        );

        when(repository.findByUserIdAndIsReadFalse(1L))
                .thenReturn(List.of(first, second));

        service.markAllAsRead(1L);

        assertThat(first.getIsRead()).isTrue();
        assertThat(second.getIsRead()).isTrue();

        verify(repository).saveAll(List.of(first, second));
    }

    @Test
    void getUnreadReturnsEmptyListWhenNoUnreadNotificationsExist() {

        when(repository.findByUserIdAndIsReadFalse(1L))
                .thenReturn(List.of());

        List<NotificationResponseDTO> result =
                service.getUnread(1L);

        assertThat(result).isEmpty();
    }

    private User user(Long id) {

        User user = new User();

        user.setId(id);
        user.setRoleId(4L);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("password");
        user.setIsActive(true);

        return user;
    }

    private Notification notification(
            Long id,
            User user,
            String message,
            String type,
            Boolean isRead
    ) {

        Notification notification = new Notification();

        notification.setId(id);
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(isRead);
        notification.setCreatedAt(LocalDateTime.now());

        return notification;
    }
}