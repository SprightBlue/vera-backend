package com.unlam.verabackend.analysis.infrastructure.entity;

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
@Table(name = "user_caregivers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_caregiver", columnNames = {"user_id", "caregiver_id"})
})
public class UserCaregiverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "caregiver_id", nullable = false)
    private Long caregiverId;

    @Column(name = "relationship_type_id", length = 50, nullable = false)
    private String relationshipTypeId;

    @Column(name = "phone", length = 50, nullable = false)
    private String phone;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
