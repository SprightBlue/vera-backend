package com.unlam.verabackend.infrastructure.entity;

import com.unlam.verabackend.domain.model.SensitivityLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


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



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensitivityLevel sensitivityLevel= SensitivityLevel.MEDIO;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;


    // Notificaciones

    @Column(nullable = false)
    private boolean notifyHighRisk = true;

    @Column(nullable = false)
    private boolean receiveAlertSummaries= false;


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
