package com.codesync.file.service;

import com.codesync.file.dto.FileRequestDto;
import com.codesync.file.dto.FileResponseDto;
import com.codesync.file.dto.FileTreeDto;
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

    @Override
    @Transactional
    public FileResponseDto createFile(FileRequestDto requestDto, String currentUserId) {
        // Inter-service call to validate project existence
        try {
            projectClient.getProjectById(requestDto.getProjectId());
        } catch (Exception e) {
            log.error("Error validating project {}: {}", requestDto.getProjectId(), e.getMessage());
            throw new ResourceNotFoundException("Cannot create file. Project not found with id: " + requestDto.getProjectId());
        }

        if (fileRepository.existsByProjectIdAndPath(requestDto.getProjectId(), requestDto.getPath())) {
            throw new IllegalArgumentException("A file or folder already exists at this path");
        }

        CodeFile codeFile = CodeFile.builder()
                .projectId(requestDto.getProjectId())
                .name(extractNameFromPath(requestDto.getPath()))
                .path(requestDto.getPath())
                .language(requestDto.getLanguage())
                .content(requestDto.isFolder() ? null : requestDto.getContent())
                .sizeBytes(requestDto.getContent() != null ? (long) requestDto.getContent().getBytes().length : 0L)
                .isFolder(requestDto.isFolder())
                .isDeleted(false)
                .createdById(currentUserId)
                .lastEditedBy(currentUserId)
                .build();

        codeFile = fileRepository.save(codeFile);
        return mapToDto(codeFile);
    }

    @Override
    public FileResponseDto getFileById(Long id) {
        CodeFile codeFile = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
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
        
        codeFile = fileRepository.save(codeFile);
        return mapToDto(codeFile);
    }

    @Override
    @Transactional
    public void deleteFile(Long id) {
        CodeFile codeFile = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        
        codeFile.setDeleted(true);
        fileRepository.save(codeFile);
    }

    @Override
    @Transactional
    public FileResponseDto restoreFile(Long id) {
        CodeFile codeFile = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        
        codeFile.setDeleted(false);
        codeFile = fileRepository.save(codeFile);
        return mapToDto(codeFile);
    }

    @Override
    public List<FileTreeDto> getProjectFileTree(Long projectId) {
        List<CodeFile> files = fileRepository.findByProjectIdAndIsDeletedFalse(projectId);
        return buildTree(files);
    }

    private List<FileTreeDto> buildTree(List<CodeFile> files) {
        FileTreeDto root = FileTreeDto.builder().name("root").path("").isFolder(true).build();
        Map<String, FileTreeDto> folderMap = new HashMap<>();
        folderMap.put("", root);

        // Sort files to ensure folders are processed before their children if possible,
        // but robust tree building handles any order by implicitly creating missing parent folders.
        for (CodeFile file : files) {
            String path = file.getPath();
            String[] parts = path.split("/");
            
            String currentPath = "";
            FileTreeDto currentParent = root;

            for (int i = 0; i < parts.length; i++) {
                currentPath = currentPath.isEmpty() ? parts[i] : currentPath + "/" + parts[i];
                
                if (!folderMap.containsKey(currentPath)) {
                    boolean isLast = (i == parts.length - 1);
                    FileTreeDto node = FileTreeDto.builder()
                            .id(isLast ? file.getId() : null) // Only assign ID to the actual file/folder record
                            .name(parts[i])
                            .path(currentPath)
                            .isFolder(!isLast || file.isFolder())
                            .language(isLast ? file.getLanguage() : null)
                            .children(new ArrayList<>())
                            .build();
                    
                    currentParent.getChildren().add(node);
                    folderMap.put(currentPath, node);
                }
                currentParent = folderMap.get(currentPath);
            }
        }

        return root.getChildren();
    }

    private String extractNameFromPath(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
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
