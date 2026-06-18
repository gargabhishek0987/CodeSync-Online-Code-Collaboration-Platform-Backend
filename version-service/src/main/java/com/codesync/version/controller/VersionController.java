package com.codesync.version.controller;

import com.codesync.version.dto.DiffResult;
import com.codesync.version.model.Snapshot;
import com.codesync.version.service.SnapshotService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/versions")
@RequiredArgsConstructor
public class VersionController {

    private final SnapshotService snapshotService;

    @PostMapping("/snapshot")
    public ResponseEntity<Snapshot> createSnapshot(@RequestBody SnapshotRequest request) {
        Snapshot snapshot = snapshotService.createSnapshot(
                request.getFileId(),
                request.getContent(),
                request.getCommitMessage(),
                request.getBranch(),
                request.getParentId(),
                request.getUserId()
        );
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping("/file/{fileId}/history")
    public ResponseEntity<List<Snapshot>> getHistory(@PathVariable Long fileId) {
        return ResponseEntity.ok(snapshotService.getHistory(fileId));
    }

    @GetMapping("/file/{fileId}/branch/{branch}")
    public ResponseEntity<List<Snapshot>> getBranchHistory(@PathVariable Long fileId, @PathVariable String branch) {
        return ResponseEntity.ok(snapshotService.getBranchHistory(fileId, branch));
    }

    @GetMapping("/diff")
    public ResponseEntity<DiffResult> getDiff(@RequestParam String v1, @RequestParam String v2) {
        return ResponseEntity.ok(snapshotService.calculateDiff(v1, v2));
    }

    @GetMapping("/{snapshotId}")
    public ResponseEntity<Snapshot> getSnapshot(@PathVariable String snapshotId) {
        return snapshotService.getSnapshot(snapshotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class SnapshotRequest {
        private Long fileId;
        private String content;
        private String commitMessage;
        private String branch;
        private String parentId;
        private String userId;
    }
}
