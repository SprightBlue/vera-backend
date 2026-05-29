package com.unlam.verabackend.infrastructure.provider;

import com.unlam.verabackend.domain.ports.out.LinkGeneratorService;
import org.springframework.stereotype.Component;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class DirectLinkGenerator implements LinkGeneratorService {

    @Override
    public String generateEmailLink(String email, String subject) {
        try {
            String encodedSubject = URLEncoder.encode(subject, StandardCharsets.UTF_8).replace("+", "%20");
            return "mailto:" + email + "?subject=" + encodedSubject;
        } catch (Exception e) {
            return "mailto:" + email;
        }
    }
}
