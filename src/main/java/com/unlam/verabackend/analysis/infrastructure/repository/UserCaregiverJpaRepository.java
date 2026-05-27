package com.unlam.verabackend.analysis.infrastructure.repository;

import com.unlam.verabackend.analysis.infrastructure.entity.UserCaregiverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCaregiverJpaRepository extends JpaRepository<UserCaregiverEntity, Long> {
	List<UserCaregiverEntity> findByUserId(Long userId);
}
