package com.codesync.project.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSynopsisDto {
    private List<String> actors;
    private List<String> useCases;
    private List<String> requirements;
}
