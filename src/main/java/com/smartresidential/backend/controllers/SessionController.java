package com.smartresidential.backend.controllers;

import com.smartresidential.backend.entities.Session;
import com.smartresidential.backend.services.interfaces.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // 🔹 GET current user sessions (më korrekt se userId nga frontend)
    @GetMapping("/me")
    public ResponseEntity<List<Session>> getMySessions(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(sessionService.getSessionsByToken(token));
    }

    // 🔹 GET session by id
    @GetMapping("/{id}")
    public ResponseEntity<Session> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(
                sessionService.getSessionById(id)
                        .orElseThrow(() -> new RuntimeException("Session not found"))
        );
    }

    // 🔹 DELETE single session (logout nga 1 pajisje)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    // 🔹 LOGOUT current session (me token – version më i mirë)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String token
    ) {
        sessionService.logout(token);
        return ResponseEntity.noContent().build();
    }

    // 🔹 LOGOUT ALL sessions (current user)
    @DeleteMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @RequestHeader("Authorization") String token
    ) {
        sessionService.logoutAllByToken(token);
        return ResponseEntity.noContent().build();
    }

    // 🔹 CHECK token validity
    @GetMapping("/validate")
    public ResponseEntity<Boolean> isSessionValid(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(sessionService.isSessionValid(token));
    }
}