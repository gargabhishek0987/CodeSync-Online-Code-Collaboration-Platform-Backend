package com.codesync.file.controller;

import com.codesync.file.dto.FileRequestDto;
import com.codesync.file.dto.FileResponseDto;
import com.codesync.file.dto.FileTreeDto;
import com.codesync.file.dto.UpdateContentDto;
import com.codesync.file.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity<FileResponseDto> createFile(@Valid @RequestBody FileRequestDto requestDto,
                                                      Authentication authentication) {
        FileResponseDto file = fileService.createFile(requestDto, authentication.getName());
        return new ResponseEntity<>(file, HttpStatus.CREATED);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<FileTreeDto>> getProjectFileTree(@PathVariable Long projectId) {
        return ResponseEntity.ok(fileService.getProjectFileTree(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponseDto> getFileById(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getFileById(id));
    }

    @PutMapping("/{id}/content")
    public ResponseEntity<FileResponseDto> updateContent(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateContentDto updateDto,
                                                         Authentication authentication) {
        FileResponseDto updatedFile = fileService.updateContent(id, updateDto, authentication.getName());
        return ResponseEntity.ok(updatedFile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "File soft-deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<FileResponseDto> restoreFile(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.restoreFile(id));
    }
}
