package com.codesync.project.dto;

import com.codesync.project.entity.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectRequestDto {

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Programming language is required")
    private String language;

    @NotNull(message = "Visibility setting is required")
    private Visibility visibility;

    private String templateId;
}
