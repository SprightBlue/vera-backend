package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Analysis;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface AnalyzeContentUseCase {
    @Transactional
    Analysis execute(String userEmail, String rawText, MultipartFile file, String source);
}