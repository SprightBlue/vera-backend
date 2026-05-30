package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.DomainUser;
import com.unlam.verabackend.domain.model.RelationshipType;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.model.UserCaregiver;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.entity.UserCaregiverEntity;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserCaregiverRepositoryImpl implements UserCaregiverRepository {

    private final UserCaregiverJpaRepository jpaRepository;

    public UserCaregiverRepositoryImpl(UserCaregiverJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<UserCaregiver> findByUserId(Long userId) {
        return jpaRepository.findAllByUserIdWithTree(userId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private UserCaregiver mapToDomain(UserCaregiverEntity entity) {
        DomainUser protectedUser = mapUserToDomain(entity.getUser());
        DomainUser caregiverUser = mapUserToDomain(entity.getCaregiver());

        return new UserCaregiver(
                entity.getId(),
                protectedUser,
                caregiverUser,
                RelationshipType.fromString(entity.getRelationshipTypeId()),
                entity.getPhone(),
                entity.getEmail(),
                entity.getCreatedAt()
        );
    }

    private DomainUser mapUserToDomain(User entity) {
        return new DomainUser(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getRole() != null ? Role.valueOf(entity.getRole().name()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isAccountNonLocked(),
                entity.isEnabled()
        );
    }
}
