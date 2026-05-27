package com.unlam.verabackend.analysis.infrastructure.repository;

import com.unlam.verabackend.analysis.domain.model.RelationshipType;
import com.unlam.verabackend.analysis.domain.model.UserCaregiver;
import com.unlam.verabackend.analysis.domain.ports.out.UserCaregiverRepositoryPort;
import com.unlam.verabackend.analysis.infrastructure.entity.UserCaregiverEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class UserCaregiverRepositoryAdapter implements UserCaregiverRepositoryPort {

    private final UserCaregiverJpaRepository jpaRepository;

    public UserCaregiverRepositoryAdapter(UserCaregiverJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(UserCaregiver domain) {
        if (domain == null) return;
        UserCaregiverEntity entity = toEntity(domain);
        jpaRepository.save(entity);
        domain.setId(entity.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCaregiver> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    private UserCaregiver toDomain(UserCaregiverEntity entity) {
        return new UserCaregiver(
                entity.getId(),
                entity.getUserId(),
                entity.getCaregiverId(),
                RelationshipType.fromString(entity.getRelationshipTypeId()),
                entity.getPhone(),
                entity.getEmail(),
                entity.getCreatedAt()
        );
    }

    private UserCaregiverEntity toEntity(UserCaregiver domain) {
        return new UserCaregiverEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getCaregiverId(),
                domain.getRelationshipType() != null ? domain.getRelationshipType().name() : RelationshipType.UNDEFINED.name(),
                domain.getPhone(),
                domain.getEmail(),
                domain.getCreatedAt()
        );
    }
}
