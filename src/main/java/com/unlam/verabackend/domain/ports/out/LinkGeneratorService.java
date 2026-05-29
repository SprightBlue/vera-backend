package com.unlam.verabackend.domain.ports.out;

public interface LinkGeneratorService {
    String generateEmailLink(String email, String subject);
}
