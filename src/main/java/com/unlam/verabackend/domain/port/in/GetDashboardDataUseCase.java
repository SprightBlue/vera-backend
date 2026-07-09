package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.DashboardData;
import com.unlam.verabackend.domain.model.Role;

public interface GetDashboardDataUseCase {
    DashboardData execute(String email, Role role);
}