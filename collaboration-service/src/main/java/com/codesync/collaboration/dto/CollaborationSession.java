package com.codesync.collaboration.dto;

import lombok.Data;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class CollaborationSession {
    private String id;
    private String projectId;
    private String fileId;
    private String ownerId;
    private Integer maxParticipants;
    private String password;
    private Set<String> activeUsers;
    private Instant lastActivity;

    public CollaborationSession() {
        this.id = UUID.randomUUID().toString();
        this.activeUsers = Collections.synchronizedSet(new HashSet<>());
        this.lastActivity = Instant.now();
    }
}
