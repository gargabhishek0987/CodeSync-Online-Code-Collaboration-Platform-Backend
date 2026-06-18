package com.codesync.version.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiffResult {
    private String snapshotId1;
    private String snapshotId2;
    private List<DiffLine> lines;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiffLine {
        private String type; // ADDED, DELETED, EQUAL
        private String content;
        private Integer oldLineNumber;
        private Integer newLineNumber;
    }
}
