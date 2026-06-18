package com.codesync.execution.service;

import com.codesync.execution.model.ExecutionJob;
import com.codesync.execution.model.JobStatus;
import com.codesync.execution.dto.ExecutionJobEvent;
import com.codesync.execution.repository.ExecutionJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionService {

    private final ExecutionJobRepository repository;
    private final ExecutionProducer executionProducer;
    private final JobProcessor jobProcessor;

    public ExecutionJob submitJob(String language, String code) {
        ExecutionJob job = ExecutionJob.builder()
                .language(language)
                .code(code)
                .status(JobStatus.PENDING)
                .build();

        ExecutionJob savedJob = repository.save(job);

        // Queue job in RabbitMQ
        try {
            ExecutionJobEvent event = new ExecutionJobEvent(
                savedJob.getId(),
                savedJob.getLanguage(),
                savedJob.getCode(),
                "unknown" // Placeholder for now
            );
            executionProducer.queueExecutionJob(event);
        } catch (Exception e) {
            log.error("Failed to queue execution job to RabbitMQ: {}. Falling back to direct async processing.", e.getMessage());
            jobProcessor.processJob(savedJob.getId());
        }

        return savedJob;
    }

    public Optional<ExecutionJob> getJobStatus(String jobId) {
        return repository.findById(jobId);
    }

    public java.util.List<ExecutionJob> getAllJobs() {
        return repository.findAll();
    }
}
