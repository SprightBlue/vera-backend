package com.unlam.verabackend.infrastructure.entity;

import com.unlam.verabackend.domain.model.RiskLevel;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;
    
    private String source;
    private LocalDateTime timestamp;

    public AlertEntity() {}

    public AlertEntity(Long id, String title, String description, RiskLevel riskLevel, String source, LocalDateTime timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.riskLevel = riskLevel;
        this.source = source;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}