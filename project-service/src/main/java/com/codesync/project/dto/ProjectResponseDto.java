package com.codesync.project.dto;

import com.codesync.project.entity.Visibility;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponseDto {
    private Long id;
    private String ownerId;
    private String name;
    private String description;
    private String language;
    private Visibility visibility;
    private String templateId;
    private boolean isArchived;
    private Integer starCount;
    private Integer forkCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProjectSynopsisDto synopsis;
}
