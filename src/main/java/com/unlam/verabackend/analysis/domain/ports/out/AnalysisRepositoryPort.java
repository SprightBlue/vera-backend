package com.unlam.verabackend.analysis.domain.ports.out;

import com.unlam.verabackend.analysis.domain.model.Analysis;

public interface AnalysisRepositoryPort {
    void save(Analysis analysis);
}
