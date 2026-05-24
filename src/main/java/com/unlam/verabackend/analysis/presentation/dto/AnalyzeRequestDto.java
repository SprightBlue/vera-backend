package com.unlam.verabackend.analysis.presentation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AnalyzeRequestDto {
    private UUID id;
    private Long userId;
    private String content;
    private String source;
}
