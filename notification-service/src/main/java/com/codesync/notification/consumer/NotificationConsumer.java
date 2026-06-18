package com.codesync.notification.consumer;

import com.codesync.notification.config.RabbitMQConfig;
import com.codesync.notification.dto.NotificationEvent;
import com.codesync.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeNotification(NotificationEvent event) {
        log.info("Received notification event from RabbitMQ: {}", event);
        
        // Process the notification (save to DB, send via WebSocket, etc.)
        notificationService.createNotification(
            event.getUserId(),
            event.getType(),
            event.getMessage(),
            event.getProjectId()
        );
    }
}
