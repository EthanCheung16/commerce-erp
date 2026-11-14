package com.example.commerce.report;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReportFilter(LocalDateTime from, LocalDateTime until) {
    public ReportFilter {
        if (from == null || until == null || !from.isBefore(until)) {
            throw new IllegalArgumentException("from 必须早于 until，结束日期不包含当天");
        }
    }

    public static ReportFilter defaults() {
        return dates(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1));
    }

    public static ReportFilter dates(LocalDate from, LocalDate until) {
        return new ReportFilter(from.atStartOfDay(), until.atStartOfDay());
    }
}
