package com.codesync.project.service;

import com.codesync.project.dto.ProjectRequestDto;
import com.codesync.project.dto.ProjectResponseDto;
import com.codesync.project.dto.ProjectSynopsisDto;
import com.codesync.project.entity.Project;
import com.codesync.project.entity.ProjectSynopsis;
import com.codesync.project.entity.Visibility;
import com.codesync.project.exception.ResourceNotFoundException;
import com.codesync.project.dto.NotificationEvent;
import com.codesync.project.repository.ProjectRepository;
import com.codesync.project.repository.ProjectSynopsisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectSynopsisRepository synopsisRepository;
    private final com.codesync.project.repository.ProjectMemberRepository memberRepository;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public void inviteUser(Long projectId, String inviterId, String inviteeId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwnerId().equals(inviterId)) {
            throw new RuntimeException("Only the owner can invite collaborators");
        }

        if (memberRepository.existsByProjectIdAndUserId(projectId, inviteeId)) {
            throw new RuntimeException("User is already a member of this project");
        }

        com.codesync.project.entity.ProjectMember member = com.codesync.project.entity.ProjectMember.builder()
                .project(project)
                .userId(inviteeId)
                .role(com.codesync.project.entity.ProjectMember.MemberRole.CONTRIBUTOR)
                .build();

        memberRepository.save(member);

        // Send Session Invite Notification
        try {
            NotificationEvent event = new NotificationEvent(
                "SESSION_INVITE",
                inviterId + " has invited you to collaborate on project: " + project.getName(),
                inviteeId,
                projectId.toString()
            );
            notificationProducer.sendNotification(event);
            log.info("Invite notification sent to {} for project {}", inviteeId, project.getName());
        } catch (Exception e) {
            log.error("Failed to send invite notification: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public ProjectResponseDto createProject(String ownerId, ProjectRequestDto requestDto) {
        Project project = Project.builder()
                .ownerId(ownerId)
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .language(requestDto.getLanguage())
                .visibility(requestDto.getVisibility())
                .templateId(requestDto.getTemplateId())
                .isArchived(false)
                .starCount(0)
                .forkCount(0)
                .build();

        project = projectRepository.save(project);
        
        // Send notification via RabbitMQ
        try {
            NotificationEvent event = new NotificationEvent(
                "PROJECT_CREATED",
                "Project '" + project.getName() + "' has been created successfully!",
                ownerId,
                project.getId().toString()
            );
            notificationProducer.sendNotification(event);
        } catch (Exception e) {
            log.error("Failed to send project creation notification: {}", e.getMessage());
        }

        return mapToDto(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getPublicProjects(String language, String query) {
        List<Project> projects;

        if (query != null && !query.isEmpty()) {
            projects = projectRepository.searchPublicProjects(query, Visibility.PUBLIC);
        } else if (language != null && !language.isEmpty()) {
            projects = projectRepository.findByLanguageIgnoreCaseAndVisibility(language, Visibility.PUBLIC);
        } else {
            projects = projectRepository.findByVisibility(Visibility.PUBLIC);
        }

        return projects.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getUserProjects(String ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponseDto getProjectById(Long id, String currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        // Basic authorization check
        if (project.getVisibility() == Visibility.PRIVATE && !project.getOwnerId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Project not found or access denied");
        }

        return mapToDto(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProject(Long id, String currentUserId, ProjectRequestDto requestDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (!project.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized to update this project");
        }

        project.setName(requestDto.getName());
        project.setDescription(requestDto.getDescription());
        project.setLanguage(requestDto.getLanguage());
        project.setVisibility(requestDto.getVisibility());
        
        project = projectRepository.save(project);
        return mapToDto(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long id, String currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (!project.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized to delete this project");
        }

        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public void deleteProjectAdmin(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto starProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        project.setStarCount(project.getStarCount() + 1);
        project = projectRepository.save(project);
        return mapToDto(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto forkProject(Long id, String newOwnerId) {
        Project sourceProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source Project not found with id: " + id));

        if (sourceProject.getVisibility() == Visibility.PRIVATE && !sourceProject.getOwnerId().equals(newOwnerId)) {
            throw new RuntimeException("Cannot fork a private project unless you are the owner");
        }

        // Increment fork count on original project
        sourceProject.setForkCount(sourceProject.getForkCount() + 1);
        projectRepository.save(sourceProject);

        // Create new project
        Project forkedProject = Project.builder()
                .ownerId(newOwnerId)
                .name(sourceProject.getName() + "-fork")
                .description("Forked from " + sourceProject.getOwnerId() + "/" + sourceProject.getName() + " | " + sourceProject.getDescription())
                .language(sourceProject.getLanguage())
                .visibility(Visibility.PUBLIC)
                .isArchived(false)
                .starCount(0)
                .forkCount(0)
                .build();

        forkedProject = projectRepository.save(forkedProject);
        
        return mapToDto(forkedProject);
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProjectSynopsis(Long id, String currentUserId, ProjectSynopsisDto synopsisDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (!project.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized to update this project synopsis");
        }

        ProjectSynopsis synopsis = synopsisRepository.findByProjectId(id)
                .orElse(ProjectSynopsis.builder().project(project).build());

        synopsis.setActors(synopsisDto.getActors());
        synopsis.setUseCases(synopsisDto.getUseCases());
        synopsis.setRequirements(synopsisDto.getRequirements());

        synopsisRepository.save(synopsis);
        project.setSynopsis(synopsis);
        
        return mapToDto(project);
    }

    private ProjectResponseDto mapToDto(Project project) {
        ProjectSynopsisDto synopsisDto = null;
        if (project.getSynopsis() != null) {
            synopsisDto = ProjectSynopsisDto.builder()
                    .actors(project.getSynopsis().getActors())
                    .useCases(project.getSynopsis().getUseCases())
                    .requirements(project.getSynopsis().getRequirements())
                    .build();
        }

        return ProjectResponseDto.builder()
                .id(project.getId())
                .ownerId(project.getOwnerId())
                .name(project.getName())
                .description(project.getDescription())
                .language(project.getLanguage())
                .visibility(project.getVisibility())
                .templateId(project.getTemplateId())
                .isArchived(project.isArchived())
                .starCount(project.getStarCount())
                .forkCount(project.getForkCount())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .synopsis(synopsisDto)
                .build();
    }
}
