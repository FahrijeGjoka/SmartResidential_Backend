package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.entities.Session;
import com.smartresidential.backend.repositories.SessionRepository;
import com.smartresidential.backend.services.interfaces.SessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
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
    public List<Session> getSessionsByUserId(Long userId) {
        return sessionRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<Session> getSessionById(Long id) {
        return sessionRepository.findById(id);
    }

    @Override
    public Optional<Session> getSessionByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    @Override
    public Session createSession(Session session) {
        return sessionRepository.save(session);
    }

    @Override
    public Session updateSession(Long id, Session session) {
        Session existingSession = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));

        existingSession.setUser(session.getUser());
        existingSession.setToken(session.getToken());
        existingSession.setExpiresAt(session.getExpiresAt());

        return sessionRepository.save(existingSession);
    }

    @Override
    public void deleteSession(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));

        sessionRepository.delete(session);
    }

    @Override
    public List<Session> getSessionsByToken(String token) {
        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Session not found with token: " + token));

        return sessionRepository.findAllByUserId(session.getUser().getId());
    }

    @Override
    public void logoutAllByToken(String token) {
        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Session not found with token: " + token));

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
        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Session not found with token: " + token));

        sessionRepository.delete(session);
    }

    @Override
    public boolean isSessionValid(String token) {
        return sessionRepository.findByTokenAndExpiresAtAfter(token, LocalDateTime.now()).isPresent();
    }
}