package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.entities.Session;

import java.util.List;
import java.util.Optional;

public interface SessionService {

    List<Session> getAllSessions();

    Optional<Session> getSessionById(Long id);

    List<Session> getSessionsByUserId(Long userId);

    List<Session> getSessionsByToken(String token);

    void deleteSession(Long id);

    void deleteSessionsByUserId(Long userId);

    void logout(String token);

    void logoutAllByToken(String token);

    boolean isSessionValid(String token);
}
