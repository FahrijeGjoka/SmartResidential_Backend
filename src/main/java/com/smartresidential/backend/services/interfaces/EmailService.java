package com.smartresidential.backend.services.interfaces;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
