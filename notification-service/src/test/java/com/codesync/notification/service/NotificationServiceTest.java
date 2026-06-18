package com.codesync.notification.service;

import com.codesync.notification.model.Notification;
import com.codesync.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id("1")
                .userId("user123")
                .type("COMMENT")
                .message("New comment")
                .isRead(false)
                .build();
    }

    @Test
    void testCreateNotification() {
        when(repository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.createNotification("user123", "COMMENT", "New comment", "ref1");

        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        verify(repository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/user123"), eq(notification));
    }

    @Test
    void testGetUserNotifications() {
        when(repository.findByUserIdOrderByCreatedAtDesc("user123")).thenReturn(Arrays.asList(notification));

        List<Notification> result = notificationService.getUserNotifications("user123");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(repository, times(1)).findByUserIdOrderByCreatedAtDesc("user123");
    }

    @Test
    void testGetUnreadCount() {
        when(repository.countByUserIdAndIsReadFalse("user123")).thenReturn(5L);

        long count = notificationService.getUnreadCount("user123");

        assertEquals(5L, count);
        verify(repository, times(1)).countByUserIdAndIsReadFalse("user123");
    }

    @Test
    void testMarkAsRead_Success() {
        when(repository.findById("1")).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.markAsRead("1");

        assertTrue(result.isRead());
        verify(repository, times(1)).findById("1");
        verify(repository, times(1)).save(notification);
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(repository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead("invalid"));
        verify(repository, times(1)).findById("invalid");
        verify(repository, never()).save(any());
    }

    @Test
    void testMarkAllAsRead() {
        Notification n1 = Notification.builder().userId("user123").isRead(false).build();
        Notification n2 = Notification.builder().userId("user123").isRead(false).build();
        when(repository.findByUserIdOrderByCreatedAtDesc("user123")).thenReturn(Arrays.asList(n1, n2));

        notificationService.markAllAsRead("user123");

        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
        verify(repository, times(1)).saveAll(any());
    }
}
