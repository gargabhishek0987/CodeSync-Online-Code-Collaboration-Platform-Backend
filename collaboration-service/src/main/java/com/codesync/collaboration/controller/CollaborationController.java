package com.codesync.collaboration.controller;

import com.codesync.collaboration.dto.CodeChangeMessage;
import com.codesync.collaboration.dto.CursorMessage;
import com.codesync.collaboration.dto.PresenceMessage;
import com.codesync.collaboration.service.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CollaborationController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionManager sessionManager;

    @MessageMapping("/session/{sessionId}/join")
    public void joinSession(@DestinationVariable String sessionId, @Payload(required = false) String password, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        if (principal == null) return;
        String username = principal.getName();
        
        boolean joined = sessionManager.addUserToSession(sessionId, username, password);
        if (!joined) {
            log.warn("User {} failed to join session {}", username, sessionId);
            return;
        }
        
        headerAccessor.getSessionAttributes().put("sessionId", sessionId);
        headerAccessor.getSessionAttributes().put("username", username);

        log.info("User {} joined session {}", username, sessionId);

        PresenceMessage msg = new PresenceMessage();
        msg.setProjectId(sessionId); // using same field for compatibility, implies session context
        msg.setUsername(username);
        msg.setStatus("JOINED");
        msg.setActiveUsers(sessionManager.getActiveUsers(sessionId));
        
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/presence", msg);
    }

    @MessageMapping("/session/{sessionId}/edit")
    public void handleEdit(@DestinationVariable String sessionId, @Payload CodeChangeMessage message) {
        sessionManager.recordActivity(sessionId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/edit", message);
    }

    @MessageMapping("/session/{sessionId}/cursor")
    public void handleCursorMove(@DestinationVariable String sessionId, @Payload CursorMessage message) {
        sessionManager.recordActivity(sessionId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/cursor", message);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        if (headerAccessor.getSessionAttributes() != null) {
            String sessionId = (String) headerAccessor.getSessionAttributes().get("sessionId");
            String username = (String) headerAccessor.getSessionAttributes().get("username");

            if (sessionId != null && username != null) {
                log.info("User {} disconnected from session {}", username, sessionId);
                sessionManager.removeUserFromSession(sessionId, username);
                
                PresenceMessage msg = new PresenceMessage();
                msg.setProjectId(sessionId);
                msg.setUsername(username);
                msg.setStatus("LEFT");
                msg.setActiveUsers(sessionManager.getActiveUsers(sessionId));
                
                messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/presence", msg);
            }
        }
    }
}
