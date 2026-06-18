package com.codesync.file.service;

import com.codesync.file.dto.FileRequestDto;
import com.codesync.file.dto.FileResponseDto;
import com.codesync.file.dto.FileTreeDto;
import com.codesync.file.dto.VersionEvent;
import com.codesync.file.dto.NotificationEvent;
import com.codesync.file.dto.UpdateContentDto;
import com.codesync.file.entity.CodeFile;
import com.codesync.file.exception.ResourceNotFoundException;
import com.codesync.file.client.ProjectClient;
import com.codesync.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final ProjectClient projectClient;
    private final VersionProducer versionProducer;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public FileResponseDto createFile(FileRequestDto requestDto, String currentUserId) {
        log.info("Creating file at path: {} in project: {} by user: {}", requestDto.getPath(), requestDto.getProjectId(), currentUserId);
        
        if (currentUserId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        // Validate project existence via Feign
        try {
            projectClient.getProjectById(requestDto.getProjectId());
        } catch (Exception e) {
            log.error("Project validation failed for ID {}: {}", requestDto.getProjectId(), e.getMessage());
            throw new ResourceNotFoundException("Project not found with id: " + requestDto.getProjectId());
        }

        String path = requestDto.getPath();

        if (fileRepository.existsByProjectIdAndPath(requestDto.getProjectId(), path)) {
            throw new IllegalArgumentException("A file or folder already exists at path: " + path);
        }

        CodeFile codeFile = CodeFile.builder()
                .projectId(requestDto.getProjectId())
                .name(extractNameFromPath(path))
                .path(path)
                .language(requestDto.getLanguage())
                .isFolder(requestDto.isFolder())
                .content(requestDto.isFolder() ? null : requestDto.getContent())
                .sizeBytes(requestDto.getContent() != null ? (long) requestDto.getContent().getBytes().length : 0L)
                .isDeleted(false)
                .createdById(currentUserId)
                .lastEditedBy(currentUserId)
                .build();

        CodeFile savedFile = fileRepository.save(codeFile);

        sendNotification(currentUserId, "FILE_CREATED", 
            "File '" + savedFile.getName() + "' created.", savedFile.getProjectId().toString());

        return mapToDto(savedFile);
    }

    @Override
    public FileResponseDto getFileById(Long id) {
        CodeFile codeFile = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        // Validate access
        try {
            projectClient.getProjectById(codeFile.getProjectId());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Project not found or access denied");
        }
        return mapToDto(codeFile);
    }

    @Override
    @Transactional
    public FileResponseDto updateContent(Long id, UpdateContentDto updateDto, String currentUserId) {
        CodeFile codeFile = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        if (codeFile.isFolder()) {
            throw new IllegalArgumentException("Cannot update content of a folder");
        }

        codeFile.setContent(updateDto.getContent());
        codeFile.setSizeBytes((long) updateDto.getContent().getBytes().length);
        codeFile.setLastEditedBy(currentUserId);
        
        CodeFile savedFile = fileRepository.save(codeFile);

        // Async Versioning
        try {
            versionProducer.sendVersionEvent(new VersionEvent(
                savedFile.getId().toString(),
                savedFile.getContent(),
                currentUserId,
                "Manual save"
            ));
        } catch (Exception e) {
            log.error("Failed to send version event for file {}: {}", id, e.getMessage());
        }

        return mapToDto(savedFile);
    }

    @Override
    @Transactional
    public void deleteFile(Long id, String currentUserId) {
        CodeFile codeFile = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        
        codeFile.setDeleted(true);
        fileRepository.save(codeFile);

        sendNotification(currentUserId, "FILE_DELETED", 
            "File '" + codeFile.getName() + "' deleted.", codeFile.getProjectId().toString());
    }

    @Override
    @Transactional
    public FileResponseDto renameFile(Long id, String newName, String currentUserId) {
        CodeFile codeFile = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        String oldName = codeFile.getName();
        String oldPath = codeFile.getPath();
        String newPath;
        
        if (oldPath.contains("/")) {
            newPath = oldPath.substring(0, oldPath.lastIndexOf("/") + 1) + newName;
        } else {
            newPath = newName;
        }

        codeFile.setName(newName);
        codeFile.setPath(newPath);
        codeFile.setLastEditedBy(currentUserId);
        
        CodeFile savedFile = fileRepository.save(codeFile);

        sendNotification(currentUserId, "FILE_RENAMED", 
            "File '" + oldName + "' renamed to '" + newName + "'.", savedFile.getProjectId().toString());

        return mapToDto(savedFile);
    }

    @Override
    @Transactional
    public FileResponseDto restoreFile(Long id) {
        CodeFile codeFile = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        
        codeFile.setDeleted(false);
        CodeFile savedFile = fileRepository.save(codeFile);
        return mapToDto(savedFile);
    }

    @Override
    public List<FileTreeDto> getProjectFileTree(Long projectId) {
        // Validate access
        try {
            projectClient.getProjectById(projectId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Project not found or access denied");
        }
        
        List<CodeFile> files = fileRepository.findByProjectIdAndIsDeletedFalse(projectId);
        return buildTree(files);
    }

    private void sendNotification(String userId, String type, String message, String projectId) {
        try {
            notificationProducer.sendNotification(new NotificationEvent(type, message, userId, projectId));
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }

    private List<FileTreeDto> buildTree(List<CodeFile> files) {
        log.info("Building tree for {} files", files.size());
        FileTreeDto root = FileTreeDto.builder().name("root").path("").isFolder(true).build();
        Map<String, FileTreeDto> nodeMap = new HashMap<>();
        nodeMap.put("", root);

        // First pass: Create nodes for all actual database records
        for (CodeFile file : files) {
            String sanitizedPath = file.getPath().replaceAll("^/+", "").replaceAll("/+$", "");
            log.info("Mapping DB record: ID={}, Path='{}'", file.getId(), sanitizedPath);
            FileTreeDto node = FileTreeDto.builder()
                    .id(file.getId())
                    .name(file.getName())
                    .path(sanitizedPath)
                    .isFolder(file.isFolder())
                    .language(file.getLanguage())
                    .children(new ArrayList<>())
                    .build();
            nodeMap.put(sanitizedPath, node);
        }

        // Second pass: Connect nodes to their parents
        for (CodeFile file : files) {
            String path = file.getPath().replaceAll("^/+", "").replaceAll("/+$", "");
            FileTreeDto node = nodeMap.get(path);
            
            int lastSlash = path.lastIndexOf("/");
            String parentPath = (lastSlash == -1) ? "" : path.substring(0, lastSlash);
            
            FileTreeDto parent = nodeMap.get(parentPath);
            if (parent != null) {
                // Check if already added to avoid duplicates
                if (parent.getChildren().stream().noneMatch(c -> c.getPath().equals(node.getPath()))) {
                    parent.getChildren().add(node);
                }
            } else {
                // If parent doesn't exist in DB (shouldn't happen with proper cleanup), 
                // attach to root as fallback
                if (root.getChildren().stream().noneMatch(c -> c.getPath().equals(node.getPath()))) {
                    root.getChildren().add(node);
                }
            }
        }
        return root.getChildren();
    }

    private String extractNameFromPath(String path) {
        if (path == null) return null;
        int lastSlash = path.lastIndexOf("/");
        return lastSlash == -1 ? path : path.substring(lastSlash + 1);
    }

    private FileResponseDto mapToDto(CodeFile codeFile) {
        return FileResponseDto.builder()
                .id(codeFile.getId())
                .projectId(codeFile.getProjectId())
                .name(codeFile.getName())
                .path(codeFile.getPath())
                .language(codeFile.getLanguage())
                .content(codeFile.getContent())
                .sizeBytes(codeFile.getSizeBytes())
                .isFolder(codeFile.isFolder())
                .isDeleted(codeFile.isDeleted())
                .createdById(codeFile.getCreatedById())
                .lastEditedBy(codeFile.getLastEditedBy())
                .createdAt(codeFile.getCreatedAt())
                .updatedAt(codeFile.getUpdatedAt())
                .build();
    }
}
