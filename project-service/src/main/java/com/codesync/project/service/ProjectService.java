package com.codesync.project.service;

import com.codesync.project.dto.ProjectRequestDto;
import com.codesync.project.dto.ProjectResponseDto;
import com.codesync.project.dto.ProjectSynopsisDto;

import java.util.List;

public interface ProjectService {
    ProjectResponseDto createProject(String ownerId, ProjectRequestDto requestDto);
    List<ProjectResponseDto> getPublicProjects(String language, String query);
    List<ProjectResponseDto> getUserProjects(String ownerId);
    ProjectResponseDto getProjectById(Long id, String currentUserId);
    ProjectResponseDto updateProject(Long id, String currentUserId, ProjectRequestDto requestDto);
    void deleteProject(Long id, String currentUserId);
    void deleteProjectAdmin(Long id);
    ProjectResponseDto starProject(Long id);
    ProjectResponseDto forkProject(Long id, String newOwnerId);
    ProjectResponseDto updateProjectSynopsis(Long id, String currentUserId, ProjectSynopsisDto synopsisDto);
    void inviteUser(Long projectId, String inviterId, String inviteeId);
}
