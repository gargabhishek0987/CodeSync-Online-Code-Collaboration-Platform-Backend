package com.codesync.file.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateContentDto {
    @NotNull(message = "Content cannot be null")
    private String content;
}
