package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.DashboardData;
import com.unlam.verabackend.domain.model.Role;
import org.springframework.transaction.annotation.Transactional;

public interface GetDashboardDataUseCase {
    @Transactional
    DashboardData execute(String email, Role role);
}