package com.codesync.collaboration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorMessage {
    private String projectId;
    private String fileId;
    private String username;
    private int lineNumber;
    private int column;
}
