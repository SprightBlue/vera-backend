package com.unlam.verabackend.domain.model;

import lombok.Getter;

@Getter
public enum Source {
    MOBILE("Movil"),
    WEB("Web");

    private final String displayName;

    Source(String displayName) {
        this.displayName = displayName;
    }

}