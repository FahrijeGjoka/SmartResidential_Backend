package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.entities.Session;

import java.util.List;
import java.util.Optional;

public interface SessionService {

    List<Session> getAllSessions();

    List<Session> getSessionsByUserId(Long userId);

    Optional<Session> getSessionById(Long id);

    Optional<Session> getSessionByToken(String token);

    Session createSession(Session session);

    Session updateSession(Long id, Session session);

    void deleteSession(Long id);

    void deleteSessionsByUserId(Long userId);

    void logout(String token);

    boolean isSessionValid(String token);
}