package com.unlam.verabackend.application.service;

public interface EmailService {

    void sendPasswordResetEmail(
            String to,
            String token
    );

    void sendVerificationEmail(String to, String token);
}