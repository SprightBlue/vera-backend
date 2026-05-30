package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.domain.model.UserCaregiver;
import java.util.List;

public interface UserCaregiverRepository {
    List<UserCaregiver> findByUserId(Long userId);
}
