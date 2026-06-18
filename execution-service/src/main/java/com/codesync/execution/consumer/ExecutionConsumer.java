package com.codesync.execution.consumer;

import com.codesync.execution.config.RabbitMQConfig;
import com.codesync.execution.dto.ExecutionJobEvent;
import com.codesync.execution.service.JobProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionConsumer {

    private final JobProcessor jobProcessor;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeExecutionJob(ExecutionJobEvent event) {
        log.info("Received execution job from RabbitMQ: {}", event.getJobId());
        
        // Hand off to the processor to do the actual Docker work
        jobProcessor.processJob(event.getJobId());
    }
}
