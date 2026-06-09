package com.unlam.verabackend.domain.port.out;

import org.springframework.web.multipart.MultipartFile;

public interface GeminiProvider {
    GeminiResult analyzeContent(String prompt, MultipartFile file);
}