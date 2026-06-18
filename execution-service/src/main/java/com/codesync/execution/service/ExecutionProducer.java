package com.codesync.execution.service;

import com.codesync.execution.config.RabbitMQConfig;
import com.codesync.execution.dto.ExecutionJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionProducer {

    private final RabbitTemplate rabbitTemplate;

    public void queueExecutionJob(ExecutionJobEvent event) {
        log.info("Queueing execution job in RabbitMQ: {}", event.getJobId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }
}
