package com.codesync.comment.service;

import com.codesync.comment.model.Comment;
import com.codesync.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository repository;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final com.codesync.comment.config.RabbitMQConfig rabbitMQConfig;

    public Comment addComment(Long fileId, Integer lineNumber, String content, String userId, String parentId) {
        Comment comment = Comment.builder()
                .fileId(fileId)
                .lineNumber(lineNumber)
                .content(content)
                .userId(userId)
                .parentId(parentId)
                .build();
        
        Comment savedComment = repository.save(comment);

        // Send Notification Event
        com.codesync.comment.dto.NotificationEvent event = com.codesync.comment.dto.NotificationEvent.builder()
                .type("NEW_COMMENT")
                .message("New comment on file " + fileId + ": " + content)
                .userId(userId) // In a real scenario, this would be the recipient
                .projectId(fileId.toString())
                .build();
        
        rabbitTemplate.convertAndSend(com.codesync.comment.config.RabbitMQConfig.EXCHANGE, 
                                     com.codesync.comment.config.RabbitMQConfig.ROUTING_KEY, 
                                     event);

        // Detect Mentions
        if (content.contains("@")) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@(\\w+)");
            java.util.regex.Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String mentionedUser = matcher.group(1);
                if (!mentionedUser.equals(userId)) {
                    com.codesync.comment.dto.NotificationEvent mentionEvent = com.codesync.comment.dto.NotificationEvent.builder()
                            .type("MENTION")
                            .message(userId + " mentioned you in a comment: " + content)
                            .userId(mentionedUser)
                            .projectId(fileId.toString())
                            .build();
                    rabbitTemplate.convertAndSend(com.codesync.comment.config.RabbitMQConfig.EXCHANGE, 
                                                 com.codesync.comment.config.RabbitMQConfig.ROUTING_KEY, 
                                                 mentionEvent);
                }
            }
        }

        return savedComment;
    }

    public List<Comment> getFileComments(Long fileId) {
        return repository.findByFileIdOrderByCreatedAtAsc(fileId);
    }

    public Comment resolveComment(String commentId, boolean resolved) {
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setResolved(resolved);
        return repository.save(comment);
    }

    public void deleteComment(String commentId) {
        repository.deleteById(commentId);
    }
}
