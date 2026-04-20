package com.codesync.project.controller;

import com.codesync.project.dto.ProjectRequestDto;
import com.codesync.project.dto.ProjectResponseDto;
import com.codesync.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto requestDto, 
                                                            Authentication authentication) {
        ProjectResponseDto project = projectService.createProject(authentication.getName(), requestDto);
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    @GetMapping("/public")
    public ResponseEntity<List<ProjectResponseDto>> getPublicProjects(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(projectService.getPublicProjects(language, query));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ProjectResponseDto>> getUserProjects(Authentication authentication) {
        return ResponseEntity.ok(projectService.getUserProjects(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long id, Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(projectService.getProjectById(id, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable Long id,
                                                            @Valid @RequestBody ProjectRequestDto requestDto,
                                                            Authentication authentication) {
        ProjectResponseDto project = projectService.updateProject(id, authentication.getName(), requestDto);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long id, Authentication authentication) {
        projectService.deleteProject(id, authentication.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Project deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/star")
    public ResponseEntity<ProjectResponseDto> starProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.starProject(id));
    }

    @PostMapping("/{id}/fork")
    public ResponseEntity<ProjectResponseDto> forkProject(@PathVariable Long id, Authentication authentication) {
        ProjectResponseDto project = projectService.forkProject(id, authentication.getName());
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }
}
