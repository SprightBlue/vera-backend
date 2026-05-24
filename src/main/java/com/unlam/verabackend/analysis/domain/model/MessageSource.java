package com.unlam.verabackend.analysis.domain.model;

import lombok.Getter;

@Getter
public enum MessageSource {
    UNKNOWN("Origen No Especificado"),
    WHATSAPP("WhatsApp"),
    TELEGRAM("Telegram");

    private final String name;

    MessageSource(String name) {
        this.name = name;
    }

}
