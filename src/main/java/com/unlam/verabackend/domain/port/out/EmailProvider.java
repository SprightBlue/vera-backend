package com.unlam.verabackend.domain.port.out;

public interface EmailProvider {

    void sendPasswordResetEmail(
            String to,
            String token
    ) throws Exception;

    void sendVerificationEmail(String to, String token) throws Exception;
}