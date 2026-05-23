package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.session.SessionResponseDTO;
import com.smartresidential.backend.entities.Session;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.services.interfaces.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@PreAuthorize("isAuthenticated()")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<SessionResponseDTO>> getMySessions(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(sessionService.getSessionsByToken(token).stream().map(this::mapToResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SessionResponseDTO> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(
                sessionService.getSessionById(id)
                        .map(this::mapToResponse)
                        .orElseThrow(() -> new ResourceNotFoundException("Session not found"))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String token
    ) {
        sessionService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @RequestHeader("Authorization") String token
    ) {
        sessionService.logoutAllByToken(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> isSessionValid(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(sessionService.isSessionValid(token));
    }

    private SessionResponseDTO mapToResponse(Session session) {
        SessionResponseDTO response = new SessionResponseDTO();
        response.setId(session.getId());
        response.setUserId(session.getUser() != null ? session.getUser().getId() : null);
        response.setExpiresAt(session.getExpiresAt());
        response.setCreatedAt(session.getCreatedAt());
        return response;
    }
}
