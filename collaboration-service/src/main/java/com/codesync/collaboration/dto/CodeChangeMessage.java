package com.codesync.collaboration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeChangeMessage {
    private String projectId;
    private String fileId;
    private String username;
    private String content; // full content for simplicity in MVP
}
