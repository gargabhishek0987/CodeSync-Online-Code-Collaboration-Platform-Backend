package com.codesync.comment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private boolean resolved = false;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "parent_id")
    private String parentId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
