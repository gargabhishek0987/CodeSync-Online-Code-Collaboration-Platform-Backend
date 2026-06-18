package com.codesync.collaboration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceMessage {
    private String projectId;
    private String username;
    private String status; // e.g., "JOINED", "LEFT"
    private Set<String> activeUsers;
}
