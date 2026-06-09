package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Analysis;
import org.springframework.web.multipart.MultipartFile;

public interface AnalyzeContentUseCase {
    Analysis execute(String userEmail, String rawText, MultipartFile file, String source);
}