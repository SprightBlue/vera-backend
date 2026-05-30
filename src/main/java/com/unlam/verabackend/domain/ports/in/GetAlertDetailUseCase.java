package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.domain.model.AlertDetail;

import java.util.UUID;

public interface GetAlertDetailUseCase {
    AlertDetail getDetail(UUID alertId, Long requestingUserId);
}