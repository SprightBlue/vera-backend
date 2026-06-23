package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface ManageIncidentsUseCase {
    Incident createIncident(String userEmail, IncidentActionType actionType, List<SharedDataType> sharedDataTypes, String description);
    Incident getIncidentDetail(UUID incidentId, String requesterEmail);
    Incident completeStep(UUID incidentId, IncidentStepKey stepKey, String userEmail);
    Page<Incident> getMyIncidents(String userEmail, Pageable pageable);
    Page<Incident> getIncidentsByTrustContact(Long trustContactId, String carerEmail, Pageable pageable);
}
