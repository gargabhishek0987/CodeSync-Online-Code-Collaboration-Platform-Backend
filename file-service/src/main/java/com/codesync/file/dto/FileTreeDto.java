package com.codesync.file.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class FileTreeDto {
    private Long id; // Null if it's an abstract root or purely a folder placeholder
    private String name;
    private String path;
    private boolean isFolder;
    private String language;
    
    @Builder.Default
    private List<FileTreeDto> children = new ArrayList<>();
}
