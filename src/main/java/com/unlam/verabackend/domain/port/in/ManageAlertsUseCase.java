package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Alerts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ManageAlertsUseCase {
    Page<Alerts> getHistoryByCarerEmail(String email, Pageable pageable);
    Page<Alerts> getHistoryByCarerEmailAndIsResolved(String email, boolean isResolved, Pageable pageable);
    Alerts getAlertDetail(UUID id, String carerEmail);
    void deleteAlert(UUID id, String carerEmail);
    void resolveAlert(UUID id, String carerEmail);
}