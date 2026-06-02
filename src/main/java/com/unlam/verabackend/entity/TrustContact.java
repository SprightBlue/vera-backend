package com.unlam.verabackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.unlam.verabackend.infrastructure.entity.User;


@Entity
@Table(name = "trust_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustContact {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carer_id", nullable = false)
    private User carer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protected_id", nullable = false)
    private User protectedUser;

    @Column(nullable = false)
    private String relationship;


    /*Definir si vamos a hacer lo del nivel de seguridad */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensitivityLevel sensitivityLevel;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /*--------------Ver si vamos a darle la opcion de seleccionar las apps a monitorear-------  */

    @Column(nullable = false)
    private boolean monitorWhatsapp;

    @Column(nullable = false)
    private boolean monitorSms;

    @Column(nullable = false)
    private boolean monitorGmail;

    @Column(nullable = false)
    private boolean monitorTelegram;

    // Notificaciones

    @Column(nullable = false)
    private boolean notifyHighRisk;

    @Column(nullable = false)
    private boolean receiveAlertSummaries;

    @Column(nullable = false)
    private boolean allowBasicConfig;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
