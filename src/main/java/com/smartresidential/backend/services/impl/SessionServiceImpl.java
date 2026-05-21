package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.entities.Session;
import com.smartresidential.backend.exceptions.BadRequestException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.repositories.SessionRepository;
import com.smartresidential.backend.services.interfaces.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public Optional<Session> getSessionById(Long id) {
        return sessionRepository.findById(id);
    }

    @Override
    public List<Session> getSessionsByUserId(Long userId) {
        return sessionRepository.findAllByUserId(userId);
    }

    @Override
    public List<Session> getSessionsByToken(String token) {
        String cleanedToken = cleanToken(token);
        Session session = sessionRepository.findByToken(cleanedToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with token: " + cleanedToken));

        return sessionRepository.findAllByUserId(session.getUser().getId());
    }

    @Override
    public void deleteSession(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));

        sessionRepository.delete(session);
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
    public void logoutAllByToken(String token) {
        String cleanedToken = cleanToken(token);
        Session session = sessionRepository.findByToken(cleanedToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with token: " + cleanedToken));

        Long userId = session.getUser().getId();
        List<Session> sessions = sessionRepository.findAllByUserId(userId);
        sessionRepository.deleteAll(sessions);
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
        if (cleanedToken.startsWith("Bearer ")) {
            cleanedToken = cleanedToken.substring(7).trim();
        }

        if (cleanedToken.isBlank()) {
            throw new BadRequestException("Authorization token is required");
        }

        return cleanedToken;
    }
}
