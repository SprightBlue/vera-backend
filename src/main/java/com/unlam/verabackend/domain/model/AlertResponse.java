package com.unlam.verabackend.domain.model;

public class AlertResponse {

    private Long id;
    private String title;
    private String risk;

    public AlertResponse(Long id, String title, String risk) {
        this.id = id;
        this.title = title;
        this.risk = risk;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getRisk() {
        return risk;
    }
}