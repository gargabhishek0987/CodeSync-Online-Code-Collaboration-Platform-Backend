package com.codesync.version.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEvent implements Serializable {
    private String type;
    private String message;
    private String userId;
    private String projectId;
}
