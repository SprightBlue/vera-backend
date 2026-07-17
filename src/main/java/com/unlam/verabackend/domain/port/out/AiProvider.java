package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.AiResult;
import com.unlam.verabackend.domain.model.ChatMessages;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface AiProvider {
    AiResult analyzeContent(String prompt, MultipartFile file);
    String generateChatResponse(String systemPrompt, List<ChatMessages> history);
}