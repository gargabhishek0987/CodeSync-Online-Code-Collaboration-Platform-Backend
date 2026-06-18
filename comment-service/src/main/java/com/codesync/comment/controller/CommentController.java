package com.codesync.comment.controller;

import com.codesync.comment.model.Comment;
import com.codesync.comment.service.CommentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody CommentRequest request) {
        Comment comment = commentService.addComment(
                request.getFileId(),
                request.getLineNumber(),
                request.getContent(),
                request.getUserId(),
                request.getParentId()
        );
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<List<Comment>> getFileComments(@PathVariable Long fileId) {
        return ResponseEntity.ok(commentService.getFileComments(fileId));
    }

    @PatchMapping("/{commentId}/resolve")
    public ResponseEntity<Comment> resolveComment(@PathVariable String commentId, @RequestParam boolean resolved) {
        return ResponseEntity.ok(commentService.resolveComment(commentId, resolved));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class CommentRequest {
        private Long fileId;
        private Integer lineNumber;
        private String content;
        private String userId;
        private String parentId;
    }
}
