package com.codesync.execution.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionJobEvent implements Serializable {
    private String jobId;
    private String language;
    private String code;
    private String userId;
}
