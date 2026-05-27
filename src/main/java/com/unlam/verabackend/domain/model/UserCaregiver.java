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
    private Long userId;
    private Long caregiverId;
    private RelationshipType relationshipType;
    private String phone;
    private String email;
    private LocalDateTime createdAt;

    public RelationshipType getRelationshipType() {
        return this.relationshipType != null ? this.relationshipType : RelationshipType.UNDEFINED;
    }
}
