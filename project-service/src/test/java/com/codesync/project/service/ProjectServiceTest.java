package com.codesync.project.service;

import com.codesync.project.dto.ProjectRequestDto;
import com.codesync.project.dto.ProjectResponseDto;
import com.codesync.project.entity.Project;
import com.codesync.project.entity.Visibility;
import com.codesync.project.exception.ResourceNotFoundException;
import com.codesync.project.repository.ProjectRepository;
import com.codesync.project.repository.ProjectSynopsisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectSynopsisRepository synopsisRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;
    private ProjectRequestDto requestDto;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id(1L)
                .ownerId("user1")
                .name("Test Project")
                .description("Description")
                .language("Java")
                .visibility(Visibility.PUBLIC)
                .starCount(0)
                .forkCount(0)
                .build();

        requestDto = new ProjectRequestDto();
        requestDto.setName("Test Project");
        requestDto.setDescription("Description");
        requestDto.setLanguage("Java");
        requestDto.setVisibility(Visibility.PUBLIC);
    }

    @Test
    void testCreateProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponseDto result = projectService.createProject("user1", requestDto);

        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(notificationProducer, times(1)).sendNotification(any());
    }

    @Test
    void testGetProjectById_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectResponseDto result = projectService.getProjectById(1L, "user1");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetProjectById_NotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(99L, "user1"));
    }

    @Test
    void testGetUserProjects() {
        when(projectRepository.findByOwnerId("user1")).thenReturn(Arrays.asList(project));

        List<ProjectResponseDto> result = projectService.getUserProjects("user1");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testDeleteProject_Success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.deleteProject(1L, "user1");

        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    void testDeleteProject_Unauthorized() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(RuntimeException.class, () -> projectService.deleteProject(1L, "user2"));
        verify(projectRepository, never()).delete(any());
    }
}
