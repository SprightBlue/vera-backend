package com.unlam.verabackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCaregiver {

    private Long id;

    // Usuario dueño del panel
    private Long userId;

    // Persona protegida
    private String protectedPersonName;

    // Relación
    private RelationshipType relationshipType;

    // Datos de contacto
    private String phone;

    private String email;

    // Configuración de protección

    // Alertas inmediatas de riesgo alto
    private Boolean highRiskAlertsEnabled;

    // Resumen semanal
    private Boolean weeklySummaryEnabled;

    // LOW | MEDIUM | HIGH
    private String notificationSensitivity;

    private LocalDateTime createdAt;

    public RelationshipType getRelationshipType() {

        return this.relationshipType != null
                ? this.relationshipType
                : RelationshipType.UNDEFINED;

    }

}