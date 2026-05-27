package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.domain.model.UserCaregiver;

import java.util.List;

public interface UserCaregiverRepositoryPort {
    void save(UserCaregiver userCaregiver);
    List<UserCaregiver> findByUserId(Long userId);
}
