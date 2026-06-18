package com.codesync.version.consumer;

import com.codesync.version.config.RabbitMQConfig;
import com.codesync.version.dto.VersionEvent;
import com.codesync.version.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VersionConsumer {

    private final SnapshotService snapshotService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeVersionEvent(VersionEvent event) {
        log.info("Received version event from RabbitMQ: {}", event);
        
        try {
            Long fileId = Long.parseLong(event.getFileId());
            snapshotService.createSnapshot(
                fileId,
                event.getContent(),
                event.getMessage(),
                "main",
                null,
                event.getUserId()
            );
            log.info("Snapshot created successfully for file: {}", fileId);
        } catch (Exception e) {
            log.error("Error processing version event: {}", e.getMessage());
        }
    }
}
