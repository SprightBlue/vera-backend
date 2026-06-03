package com.unlam.verabackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.unlam.verabackend.infrastructure.entity.User;

@Entity
@Table(name = "trust_invitations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carer_id", nullable = false)
    private User carer;

    @Column(nullable = false)
    private String fullName; 

    @Column(nullable = true)
    private String contactNumber; 

    @Column(nullable = true)
    private String email; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(nullable = false)
    private String relationship;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensitivityLevel sensitivityLevel;

    private boolean notifyHighRisk;
    private boolean receiveAlertSummaries;

//    private boolean allowBasicConfig;
//    private boolean monitorWhatsapp;
//    private boolean monitorSms;
//    private boolean monitorGmail;
//    private boolean monitorTelegram;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusDays(1); 
        status = InvitationStatus.PENDING;
    }
}