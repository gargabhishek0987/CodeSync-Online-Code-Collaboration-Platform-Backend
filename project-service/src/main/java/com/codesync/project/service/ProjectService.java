package com.codesync.project.service;

import com.codesync.project.dto.ProjectRequestDto;
import com.codesync.project.dto.ProjectResponseDto;

import java.util.List;

public interface ProjectService {
    ProjectResponseDto createProject(String ownerId, ProjectRequestDto requestDto);
    List<ProjectResponseDto> getPublicProjects(String language, String query);
    List<ProjectResponseDto> getUserProjects(String ownerId);
    ProjectResponseDto getProjectById(Long id, String currentUserId);
    ProjectResponseDto updateProject(Long id, String currentUserId, ProjectRequestDto requestDto);
    void deleteProject(Long id, String currentUserId);
    ProjectResponseDto starProject(Long id);
    ProjectResponseDto forkProject(Long id, String newOwnerId);
}
