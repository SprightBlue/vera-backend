package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.domain.model.Analysis;

public interface AnalysisRepositoryPort {
    void save(Analysis analysis);
}
