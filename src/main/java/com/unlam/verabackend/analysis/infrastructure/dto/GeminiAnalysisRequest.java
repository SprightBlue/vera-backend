package com.unlam.verabackend.analysis.infrastructure.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GeminiAnalysisRequest {
    private String messageContent;
    private boolean hasUrl;
    private boolean urlMalicious;

    public GeminiAnalysisRequest(String messageContent, boolean hasUrl, boolean urlMalicious) {
        this.messageContent = messageContent;
        this.hasUrl = hasUrl;
        this.urlMalicious = urlMalicious;
    }

}

