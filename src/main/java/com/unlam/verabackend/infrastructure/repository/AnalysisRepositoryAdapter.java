package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.AnalysisMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AnalysisRepositoryAdapter implements AnalysisRepository {

    private final JpaAnalysisRepository jpaRepository;
    private final UserRepository userRepository;
    private final AnalysisMapper mapper;

    public AnalysisRepositoryAdapter(JpaAnalysisRepository jpaRepository,
                                     UserRepository userRepository,
                                     AnalysisMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public Analysis save(Analysis analysis) {
        String emailFromDomain = analysis.getUser().getEmail();

        User realUser = userRepository.findByEmail(emailFromDomain)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en la BD con email: " + emailFromDomain));

        AnalysisEntity entity = mapper.toEntity(analysis, realUser);
        AnalysisEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
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
    public Page<Analysis> findByUserEmailAndRiskLevelOrderByCreatedAtDesc(String email, String riskLevel, Pageable pageable) {
        return jpaRepository.findByUserEmailAndRiskLevel(email, riskLevel, pageable)
                .map(mapper::toDomain);
    }
}