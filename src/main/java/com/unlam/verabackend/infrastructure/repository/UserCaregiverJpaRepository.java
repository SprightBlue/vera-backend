package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.UserCaregiverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCaregiverJpaRepository extends JpaRepository<UserCaregiverEntity, Long> {
	List<UserCaregiverEntity> findByUserId(Long userId);
}
