package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.UserCaregiver;
import java.util.List;

public interface UserCaregiverRepository {
    List<UserCaregiver> findByUserId(Long userId);
}
