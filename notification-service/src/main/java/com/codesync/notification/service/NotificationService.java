package com.codesync.notification.service;

import com.codesync.notification.model.Notification;
import com.codesync.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;
    private final org.springframework.mail.javamail.JavaMailSender mailSender;
    private final com.codesync.notification.client.AuthClient authClient;

    public Notification createNotification(String userId, String type, String message, String referenceId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .referenceId(referenceId)
                .build();

        Notification savedNotification = repository.save(notification);

        // Send real-time update via a specific topic for the user
        log.info("Sending real-time notification to topic: /topic/notifications/{}", userId);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, savedNotification);

        // Send Email Notification (Async)
        sendEmailAsync(userId, type, message);

        return savedNotification;
    }

    @org.springframework.scheduling.annotation.Async
    protected void sendEmailAsync(String username, String type, String message) {
        try {
            com.codesync.notification.client.AuthClient.UserDto user = authClient.getUserByUsername(username);
            if (user != null && user.getEmail() != null) {
                org.springframework.mail.SimpleMailMessage mailMessage = new org.springframework.mail.SimpleMailMessage();
                mailMessage.setTo(user.getEmail());
                mailMessage.setSubject("CodeSync: " + type.replace("_", " ").toUpperCase());
                
                String emailBody = String.format(
                    "Hello %s,\n\n" +
                    "You have a new notification on CodeSync:\n\n" +
                    "Type: %s\n" +
                    "Message: %s\n\n" +
                    "Check it out here: http://localhost:4200/notifications\n\n" +
                    "Best regards,\n" +
                    "The CodeSync Team",
                    username, type.replace("_", " "), message
                );
                
                mailMessage.setText(emailBody);
                mailMessage.setFrom("gargabhishek742@gmail.com");
                mailSender.send(mailMessage);
                log.info("Email notification sent to {} ({})", username, user.getEmail());
            } else {
                log.warn("Could not find email for user: {}", username);
            }
        } catch (Exception e) {
            log.error("Failed to send email notification to {}: {}", username, e.getMessage());
        }
    }

    public List<Notification> getUserNotifications(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(String userId) {
        return repository.countByUserIdAndIsReadFalse(userId);
    }

    public Notification markAsRead(String notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return repository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> unread = repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> !n.isRead())
                .toList();
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
    }
}
