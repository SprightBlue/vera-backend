package com.unlam.verabackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_caregivers")
public class UserCaregiverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario dueño del panel
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Persona protegida
    @Column(name = "protected_person_name", nullable = false)
    private String protectedPersonName;

    // Relación
    @Column(name = "relationship_type_id", nullable = false)
    private String relationshipTypeId;

    // Contacto
    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    // Configuración

    @Column(name = "high_risk_alerts_enabled")
    private Boolean highRiskAlertsEnabled;

    @Column(name = "weekly_summary_enabled")
    private Boolean weeklySummaryEnabled;

    @Column(name = "notification_sensitivity")
    private String notificationSensitivity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}