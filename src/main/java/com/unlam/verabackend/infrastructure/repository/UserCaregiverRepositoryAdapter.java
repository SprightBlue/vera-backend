package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.RelationshipType;
import com.unlam.verabackend.domain.model.UserCaregiver;
import com.unlam.verabackend.domain.ports.out.UserCaregiverRepositoryPort;
import com.unlam.verabackend.infrastructure.entity.UserCaregiverEntity;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class UserCaregiverRepositoryAdapter implements UserCaregiverRepositoryPort {

    private final UserCaregiverJpaRepository jpaRepository;

    public UserCaregiverRepositoryAdapter(
            UserCaregiverJpaRepository jpaRepository
    ) {
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

        return jpaRepository
                .findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

   private UserCaregiver toDomain(UserCaregiverEntity entity) {

    return new UserCaregiver(
            entity.getId(),
            entity.getUserId(),
            entity.getFullName(),
            RelationshipType.fromString(
                    entity.getRelationshipTypeId()
            ),
            entity.getPhone(),
            entity.getEmail(),
            entity.getHighRiskAlertsEnabled(),
            entity.getWeeklySummaryEnabled(),
            entity.getNotificationSensitivity(),
            entity.getCreatedAt()
    );

}



   private UserCaregiverEntity toEntity(UserCaregiver domain) {

    return new UserCaregiverEntity(
            domain.getId(),
            domain.getUserId(),
            domain.getFullName(),
            domain.getRelationshipType() != null
                    ? domain.getRelationshipType().name()
                    : RelationshipType.UNDEFINED.name(),
            domain.getPhone(),
            domain.getEmail(),
            domain.getHighRiskAlertsEnabled(),
            domain.getWeeklySummaryEnabled(),
            domain.getNotificationSensitivity(),
            domain.getCreatedAt()
    );



}
}