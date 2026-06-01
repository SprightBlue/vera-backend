package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.domain.model.Analysis;
import java.util.Optional;

public interface AnalysisRepository {
    void save(Analysis analysis);
    Optional<Analysis> findById(String id);
}
