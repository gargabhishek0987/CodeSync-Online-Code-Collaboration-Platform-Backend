package com.codesync.version.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionEvent implements Serializable {
    private String fileId;
    private String content;
    private String userId;
    private String message;
}
