package com.codesync.file.service;

import com.codesync.file.config.RabbitMQConfig;
import com.codesync.file.dto.VersionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VersionProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendVersionEvent(VersionEvent event) {
        log.info("Sending version event to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }
}
