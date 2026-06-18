package com.codesync.version.service;

import com.codesync.version.dto.DiffResult;
import com.codesync.version.model.Snapshot;
import com.codesync.version.repository.SnapshotRepository;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final SnapshotRepository repository;

    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public Snapshot createSnapshot(Long fileId, String content, String commitMessage, String branch, String parentId, String userId) {
        Snapshot snapshot = Snapshot.builder()
                .fileId(fileId)
                .content(content)
                .commitMessage(commitMessage)
                .branch(branch != null ? branch : "main")
                .parentId(parentId)
                .build();
        
        Snapshot savedSnapshot = repository.save(snapshot);

        // Send Notification Event
        if (userId != null) {
            com.codesync.version.dto.NotificationEvent event = com.codesync.version.dto.NotificationEvent.builder()
                    .type("SNAPSHOT_CREATED")
                    .message("New snapshot created for file " + fileId + ": " + commitMessage)
                    .userId(userId)
                    .projectId(fileId.toString())
                    .build();
            
            rabbitTemplate.convertAndSend(com.codesync.version.config.RabbitMQConfig.EXCHANGE, 
                                         com.codesync.version.config.RabbitMQConfig.ROUTING_KEY, 
                                         event);
        }

        return savedSnapshot;
    }

    public List<Snapshot> getHistory(Long fileId) {
        return repository.findByFileIdOrderByCreatedAtDesc(fileId);
    }

    public List<Snapshot> getBranchHistory(Long fileId, String branch) {
        return repository.findByFileIdAndBranchOrderByCreatedAtDesc(fileId, branch);
    }

    public Optional<Snapshot> getSnapshot(String id) {
        return repository.findById(id);
    }

    public DiffResult calculateDiff(String id1, String id2) {
        Snapshot s1 = repository.findById(id1).orElseThrow(() -> new RuntimeException("Snapshot 1 not found"));
        Snapshot s2 = repository.findById(id2).orElseThrow(() -> new RuntimeException("Snapshot 2 not found"));

        List<String> lines1 = Arrays.asList(s1.getContent().split("\n"));
        List<String> lines2 = Arrays.asList(s2.getContent().split("\n"));

        Patch<String> patch = DiffUtils.diff(lines1, lines2);
        List<DiffResult.DiffLine> diffLines = new ArrayList<>();

        // This is a simplified diff mapping for demonstration
        // In a real app, we'd iterate through the patch deltas and reconstruct the full view
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            if (delta.getType() == DeltaType.INSERT) {
                for (String line : delta.getTarget().getLines()) {
                    diffLines.add(new DiffResult.DiffLine("ADDED", line, null, delta.getTarget().getPosition()));
                }
            } else if (delta.getType() == DeltaType.DELETE) {
                for (String line : delta.getSource().getLines()) {
                    diffLines.add(new DiffResult.DiffLine("DELETED", line, delta.getSource().getPosition(), null));
                }
            } else if (delta.getType() == DeltaType.CHANGE) {
                for (String line : delta.getSource().getLines()) {
                    diffLines.add(new DiffResult.DiffLine("DELETED", line, delta.getSource().getPosition(), null));
                }
                for (String line : delta.getTarget().getLines()) {
                    diffLines.add(new DiffResult.DiffLine("ADDED", line, null, delta.getTarget().getPosition()));
                }
            }
        }

        return new DiffResult(id1, id2, diffLines);
    }
}
