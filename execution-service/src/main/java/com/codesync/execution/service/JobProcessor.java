package com.codesync.execution.service;

import com.codesync.execution.model.ExecutionJob;
import com.codesync.execution.model.JobStatus;
import com.codesync.execution.repository.ExecutionJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobProcessor {

    private final ExecutionJobRepository repository;
    private final DockerService dockerService;

    @Async
    public void processJob(String jobId) {
        log.info("Starting async processing for job ID: {} on thread: {}", jobId, Thread.currentThread().getName());

        repository.findById(jobId).ifPresent(job -> {
            try {
                job.setStatus(JobStatus.RUNNING);
                job.setStartedAt(LocalDateTime.now());
                repository.save(job);

                Map<String, String> result = dockerService.executeCode(job.getLanguage(), job.getCode());

                job.setStdout(result.get("stdout"));
                job.setStderr(result.get("stderr"));
                job.setStatus(JobStatus.COMPLETED);
            } catch (Exception e) {
                log.error("Error executing job {}: {}", jobId, e.getMessage());
                job.setStderr(e.getMessage());
                job.setStatus(JobStatus.FAILED);
            } finally {
                job.setFinishedAt(LocalDateTime.now());
                repository.save(job);
                log.info("Finished async processing for job ID: {}", jobId);
            }
        });
    }
}
