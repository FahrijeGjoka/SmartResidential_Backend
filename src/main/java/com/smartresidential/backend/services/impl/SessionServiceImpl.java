package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.entities.Session;
import com.smartresidential.backend.exceptions.BadRequestException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.SessionRepository;
import com.smartresidential.backend.services.interfaces.SessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private static final String BEARER_PREFIX = "Bearer ";

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public List<Session> getSessionsByUserId(Long userId) {
        return sessionRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<Session> getSessionById(Long id) {
        return sessionRepository.findById(id);
    }

    @Override
    public Optional<Session> getSessionByToken(String token) {
        return sessionRepository.findByToken(cleanToken(token));
    }

    @Override
    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    @Override
    public Session updateSession(Long id, Session session) {
        Session existingSession = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        existingSession.setUser(session.getUser());
        existingSession.setToken(session.getToken());
        existingSession.setExpiresAt(session.getExpiresAt());

        return sessionRepository.save(existingSession);
    }

    @Override
    public void deleteSession(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        sessionRepository.delete(session);
    }

    @Override
    public List<Session> getSessionsByToken(String token) {
        String cleanedToken = cleanToken(token);
        Session session = sessionRepository.findByToken(cleanedToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with token: " + cleanedToken));

        return sessionRepository.findAllByUserId(session.getUser().getId());
    }

    @Override
    public void logoutAllByToken(String token) {
        String cleanedToken = cleanToken(token);
        Session session = sessionRepository.findByToken(cleanedToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with token: " + cleanedToken));

        Long userId = session.getUser().getId();

        List<Session> sessions = sessionRepository.findAllByUserId(userId);
        sessionRepository.deleteAll(sessions);
    }

    @Override
    public void deleteSessionsByUserId(Long userId) {
        List<Session> sessions = sessionRepository.findAllByUserId(userId);
        sessionRepository.deleteAll(sessions);
    }

    @Override
    public void logout(String token) {
        String cleanedToken = cleanToken(token);
        Session session = sessionRepository.findByToken(cleanedToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with token: " + cleanedToken));

        sessionRepository.delete(session);
    }

    @Override
    public boolean isSessionValid(String token) {
        return sessionRepository.findByTokenAndExpiresAtAfter(cleanToken(token), LocalDateTime.now()).isPresent();
    }

    private String cleanToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("Authorization token is required");
        }

        String cleanedToken = token.trim();
        if (cleanedToken.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            cleanedToken = cleanedToken.substring(BEARER_PREFIX.length()).trim();
        }

        if (cleanedToken.isEmpty()) {
            throw new BadRequestException("Authorization token is required");
        }

        return cleanedToken;
    }
}
