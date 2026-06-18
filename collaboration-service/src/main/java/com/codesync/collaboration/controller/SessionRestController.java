package com.codesync.collaboration.controller;

import com.codesync.collaboration.dto.CollaborationSession;
import com.codesync.collaboration.service.SessionManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/collaboration/sessions")
@RequiredArgsConstructor
public class SessionRestController {

    private final SessionManager sessionManager;

    @PostMapping
    public ResponseEntity<CollaborationSession> createSession(@RequestBody SessionCreateRequest request, Principal principal) {
        String ownerId = principal != null ? principal.getName() : "anonymous";
        CollaborationSession session = sessionManager.createSession(
                request.getProjectId(),
                request.getFileId(),
                ownerId,
                request.getMaxParticipants(),
                request.getPassword()
        );
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<CollaborationSession> getSession(@PathVariable String sessionId) {
        return sessionManager.getSession(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

@Data
class SessionCreateRequest {
    private String projectId;
    private String fileId;
    private Integer maxParticipants;
    private String password;
}
