package com.codesync.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileRequestDto {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Path is required")
    private String path;

    private String language;

    private String content;

    private boolean isFolder;
}
