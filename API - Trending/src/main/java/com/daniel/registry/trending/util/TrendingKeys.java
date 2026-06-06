package com.daniel.registry.trending.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

public final class TrendingKeys {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    private TrendingKeys() {}

    public static String daily(LocalDate date) {
        return "trending:ideas:day:" + date.format(DAY_FMT);
    }

    public static String weekly(LocalDate date) {
        WeekFields wf = WeekFields.of(Locale.getDefault());
        int week = date.get(wf.weekOfWeekBasedYear());
        int weekYear = date.get(wf.weekBasedYear());
        return String.format("trending:ideas:week:%dW%02d", weekYear, week);
    }

    public static ZoneId zone() {
        // Se quiser travar em um timezone específico, altere aqui.
        return ZoneId.systemDefault();
    }
}
