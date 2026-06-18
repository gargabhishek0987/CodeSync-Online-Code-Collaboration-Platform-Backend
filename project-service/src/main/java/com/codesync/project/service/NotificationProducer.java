package com.codesync.project.service;

import com.codesync.project.config.RabbitMQConfig;
import com.codesync.project.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(NotificationEvent event) {
        log.info("Sending notification event to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }
}
