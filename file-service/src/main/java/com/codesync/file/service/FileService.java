package com.codesync.file.service;

import com.codesync.file.dto.FileRequestDto;
import com.codesync.file.dto.FileResponseDto;
import com.codesync.file.dto.FileTreeDto;
import com.codesync.file.dto.UpdateContentDto;

import java.util.List;

public interface FileService {
    FileResponseDto createFile(FileRequestDto requestDto, String currentUserId);
    FileResponseDto getFileById(Long id);
    FileResponseDto updateContent(Long id, UpdateContentDto updateDto, String currentUserId);
    void deleteFile(Long id);
    FileResponseDto restoreFile(Long id);
    List<FileTreeDto> getProjectFileTree(Long projectId);
}
