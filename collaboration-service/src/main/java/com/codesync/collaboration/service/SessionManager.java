package com.codesync.collaboration.service;

import com.codesync.collaboration.dto.CollaborationSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SessionManager {
    // Map of sessionId -> CollaborationSession
    private final Map<String, CollaborationSession> activeSessions = new ConcurrentHashMap<>();

    public CollaborationSession createSession(String projectId, String fileId, String ownerId, Integer maxParticipants, String password) {
        CollaborationSession session = new CollaborationSession();
        session.setProjectId(projectId);
        session.setFileId(fileId);
        session.setOwnerId(ownerId);
        session.setMaxParticipants(maxParticipants);
        session.setPassword(password);
        activeSessions.put(session.getId(), session);
        log.info("Created new collaboration session {} for project {}", session.getId(), projectId);
        return session;
    }

    public Optional<CollaborationSession> getSession(String sessionId) {
        return Optional.ofNullable(activeSessions.get(sessionId));
    }

    public boolean addUserToSession(String sessionId, String username, String password) {
        CollaborationSession session = activeSessions.get(sessionId);
        if (session == null) return false;

        // Check password if set
        if (session.getPassword() != null && !session.getPassword().isEmpty() && !session.getPassword().equals(password)) {
            return false;
        }

        // Check max participants
        if (session.getMaxParticipants() != null && session.getActiveUsers().size() >= session.getMaxParticipants()) {
            return false;
        }

        session.getActiveUsers().add(username);
        session.setLastActivity(Instant.now());
        return true;
    }

    public void removeUserFromSession(String sessionId, String username) {
        CollaborationSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.getActiveUsers().remove(username);
            session.setLastActivity(Instant.now());
        }
    }

    public Set<String> getActiveUsers(String sessionId) {
        CollaborationSession session = activeSessions.get(sessionId);
        return session != null ? session.getActiveUsers() : Collections.emptySet();
    }

    public void recordActivity(String sessionId) {
        CollaborationSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.setLastActivity(Instant.now());
        }
    }

    public Map<String, CollaborationSession> getAllSessions() {
        return activeSessions;
    }

    public void terminateSession(String sessionId) {
        activeSessions.remove(sessionId);
        log.info("Session {} terminated", sessionId);
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupInactiveSessions() {
        Instant threshold = Instant.now().minus(30, ChronoUnit.MINUTES);
        activeSessions.entrySet().removeIf(entry -> {
            boolean inactive = entry.getValue().getLastActivity().isBefore(threshold);
            if (inactive) {
                log.info("Cleaning up inactive session {}", entry.getKey());
            }
            return inactive;
        });
    }
}
