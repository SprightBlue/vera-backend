package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.AnalysisMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
        String emailFromDomain = analysis.getUser().getEmail();

        User realUser = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", emailFromDomain)
                .getSingleResult();

        AnalysisEntity entity = mapper.toEntity(analysis, realUser);
        AnalysisEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        if (!jpaRepository.existsById(id)) {
            throw new IllegalArgumentException("No se puede eliminar. Análisis no encontrado con ID: " + id);
        }
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<Analysis> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Analysis> findByUserEmailOrderByCreatedAtDesc(String email, Pageable pageable) {
        return jpaRepository.findByUserEmail(email, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Analysis> findByUserEmailAndRiskLevelOrderByCreatedAtDesc(String email, RiskLevel riskLevel, Pageable pageable) {
        return jpaRepository.findByUserEmailAndRiskLevel(email, riskLevel, pageable)
                .map(mapper::toDomain);
    }
}