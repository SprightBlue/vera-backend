package com.unlam.verabackend.domain.port.out;

public interface EmailService {

    void sendPasswordResetEmail(
            String to,
            String token
    );

    void sendVerificationEmail(String to, String token);
}