package com.codesync.execution.controller;

import com.codesync.execution.dto.ExecutionRequest;
import com.codesync.execution.model.ExecutionJob;
import com.codesync.execution.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping
    public ResponseEntity<ExecutionJob> execute(@RequestBody ExecutionRequest request) {
        ExecutionJob job = executionService.submitJob(request.getLanguage(), request.getCode());
        return ResponseEntity.accepted().body(job);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ExecutionJob> getStatus(@PathVariable String jobId) {
        return executionService.getJobStatus(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/all")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<ExecutionJob>> getAllJobs() {
        return ResponseEntity.ok(executionService.getAllJobs());
    }
}
