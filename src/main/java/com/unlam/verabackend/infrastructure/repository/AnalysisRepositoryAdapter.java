package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.AnalysisMapper;
import com.unlam.verabackend.infrastructure.repository.jpa.JpaAnalysisRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AnalysisRepositoryAdapter implements AnalysisRepository {

    private final JpaAnalysisRepository jpaRepository;
    private final AnalysisMapper mapper;
    private final EntityManager entityManager;

    @Override
    public Analysis save(Analysis analysis) {
        User userProxy = entityManager.getReference(User.class, analysis.getUser().getId());
        AnalysisEntity entity = mapper.toEntity(analysis, userProxy);
        AnalysisEntity savedEntity = jpaRepository.save(entity);
        entityManager.flush();
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        if (!jpaRepository.existsById(id))
            throw new IllegalArgumentException("No se puede eliminar. Análisis no encontrado con ID: " + id);
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<Analysis> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Analysis> findByCriteria(String email, RiskLevel riskLevel, String search, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return jpaRepository.filterAnalysis(email, riskLevel, cleanSearch, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<Analysis> findTop3ByUserEmail(String email) {
        return jpaRepository.findTop3ByUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserEmailSince(String email, LocalDateTime since) {
        return jpaRepository.countByUserEmailAndCreatedAtAfter(email, since);
    }
}