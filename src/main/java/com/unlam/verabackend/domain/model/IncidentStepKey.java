package com.unlam.verabackend.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IncidentStepKey {
    BLOCK_CARD(
            "Bloqueá tu tarjeta ahora",
            "Llamá a tu banco o ingresá a tu app y bloqueá la tarjeta. Banco Nación: 0810-666-4444."
    ),
    CHANGE_HOMEBANKING_PASSWORD(
            "Cambiá tu clave de homebanking",
            "Ingresá al homebanking y cambiá tu contraseña por una que no hayas usado antes."
    ),
    CHANGE_CARD_PIN(
            "Cambiá el PIN de tu tarjeta",
            "Cambiá el PIN desde la app del banco o en un cajero automático."
    ),
    CHANGE_EMAIL_PASSWORD(
            "Cambiá tu contraseña de email",
            "Cambiá la contraseña de tu correo y activá la verificación en dos pasos."
    ),
    REVIEW_MOVEMENTS(
            "Revisá tus movimientos",
            "Revisá los últimos movimientos. Si hay cargos no reconocidos, contactá al banco."
    ),
    FILE_REPORT_BANK(
            "Hacé la denuncia en el banco",
            "Reportá el incidente en tu banco por teléfono, app o sucursal."
    ),
    FILE_REPORT_POLICE(
            "Hacé la denuncia en la comisaría",
            "Hacé la denuncia en la comisaría o llamá al 137. Guardá el número de denuncia."
    ),
    CONTACT_BANK_URGENTLY(
            "Contactá a tu banco urgente",
            "Comunicáte con tu banco de urgencia. Cuanto antes, más chances de recuperar el dinero."
    ),
    SCAN_DEVICE(
            "Revisá tu dispositivo",
            "Revisá tu celular o computadora con un antivirus o pedí ayuda a alguien de confianza."
    ),
    NOTIFY_CONTACTS(
            "Avisá a tus contactos",
            "Avisá a familiares y contactos de confianza para que estén atentos."
    ),
    MONITOR_ACCOUNTS(
            "Monitoreá tus cuentas",
            "Durante las próximas semanas, revisá tus cuentas regularmente."
    ),
    LOCK_DNI(
            "Bloqueá tu DNI",
            "Comunicáte con el RENAPER al 0800-888-0188 para reportar el uso indebido de tu DNI."
    );

    private final String title;
    private final String description;
}
