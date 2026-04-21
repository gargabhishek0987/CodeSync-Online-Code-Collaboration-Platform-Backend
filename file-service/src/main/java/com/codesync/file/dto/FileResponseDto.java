package com.codesync.file.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileResponseDto {
    private Long id;
    private Long projectId;
    private String name;
    private String path;
    private String language;
    private String content;
    private Long sizeBytes;
    private boolean isFolder;
    private boolean isDeleted;
    private String createdById;
    private String lastEditedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
