package com.daniel.registry.trending.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;

public final class TrendingKeys {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final WeekFields WEEK_FIELDS = WeekFields.ISO;

    private TrendingKeys() {}

    public static String daily(LocalDate date) {
        return "trending:ideas:day:" + date.format(DAY_FMT);
    }

    public static String weekly(LocalDate date) {
        int week = date.get(WEEK_FIELDS.weekOfWeekBasedYear());
        int weekYear = date.get(WEEK_FIELDS.weekBasedYear());
        return String.format("trending:ideas:week:%dW%02d", weekYear, week);
    }

    public static ZoneId zone() {
        return ZoneOffset.UTC;
    }
}
