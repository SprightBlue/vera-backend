package com.unlam.verabackend.domain.model;

import lombok.Getter;

@Getter
public enum MessageSource {
    UNKNOWN("Origen Desconocido"),
    WHATSAPP("WhatsApp"),
    TELEGRAM("Telegram");

    private final String displayName;

    MessageSource(String displayName) { this.displayName = displayName; }

    public static MessageSource fromString(String value) {
        if (value == null) return UNKNOWN;
        for (MessageSource source : MessageSource.values()) {
            if (source.name().equalsIgnoreCase(value.trim())) return source;
        }
        return UNKNOWN;
    }
}
