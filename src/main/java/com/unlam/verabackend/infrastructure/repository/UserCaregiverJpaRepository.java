package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.UserCaregiverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserCaregiverJpaRepository extends JpaRepository<UserCaregiverEntity, Long> {

    @Query("SELECT uc FROM UserCaregiverEntity uc " +
            "JOIN FETCH uc.user u " +
            "JOIN FETCH uc.caregiver c " +
            "WHERE u.id = :userId")
    List<UserCaregiverEntity> findAllByUserIdWithTree(@Param("userId") Long userId);
}
