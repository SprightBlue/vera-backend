package com.unlam.verabackend.presentation.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateFormatter {

    public static String formatRelativeDate(LocalDateTime date) {
        if (date == null) return null;

        LocalDateTime now = LocalDateTime.now();

        long minutes = ChronoUnit.MINUTES.between(date, now);
        if (minutes < 1) return "Recién ahora";
        if (minutes < 60) return "Hace " + minutes + " min";

        long hours = ChronoUnit.HOURS.between(date, now);
        if (hours < 24 && date.getDayOfYear() == now.getDayOfYear()) {
            return "Hace " + hours + (hours == 1 ? " hora" : " horas");
        }

        long days = ChronoUnit.DAYS.between(date, now);
        if (days == 0 || (days == 1 && date.getDayOfYear() != now.getDayOfYear())) {
            return "Ayer";
        }
        if (days < 7) return "Hace " + days + " día" + (days == 1 ? "" : "s");

        long weeks = ChronoUnit.WEEKS.between(date, now);
        if (weeks < 4) return "Hace " + weeks + (weeks == 1 ? " semana" : " semanas");

        long months = ChronoUnit.MONTHS.between(date, now);
        if (months < 12) return "Hace " + months + (months == 1 ? " mes" : " meses");

        long years = ChronoUnit.YEARS.between(date, now);
        return "Hace " + years + (years == 1 ? " año" : " años");
    }
}