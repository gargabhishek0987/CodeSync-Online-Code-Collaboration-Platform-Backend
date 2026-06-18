package com.codesync.file.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "code_files", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "path"})
})
public class CodeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String path;

    @Column(length = 50)
    private String language;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "size_bytes", nullable = false)
    @Builder.Default
    private Long sizeBytes = 0L;

    @Column(name = "is_folder", nullable = false)
    private boolean isFolder;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_by_id", nullable = false)
    private String createdById;

    @Column(name = "last_edited_by")
    private String lastEditedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
