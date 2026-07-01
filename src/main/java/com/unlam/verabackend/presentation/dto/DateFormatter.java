package com.unlam.verabackend.presentation.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateFormatter {

    public static String formatRelativeDate(LocalDateTime date) {
        if (date == null) return null;

        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(date, now);

        if (days == 0) return "Hoy";
        if (days < 7) return "Hace " + days + " día" + (days == 1 ? "" : "s");

        long weeks = ChronoUnit.WEEKS.between(date, now);
        if (weeks < 4) return "Hace " + weeks + (weeks == 1 ? " semana" : " semanas");

        long months = ChronoUnit.MONTHS.between(date, now);
        if (months < 12) return "Hace " + months + (months == 1 ? " mes" : " meses");

        long years = ChronoUnit.YEARS.between(date, now);
        return "Hace " + years + (years == 1 ? " año" : " años");
    }
}