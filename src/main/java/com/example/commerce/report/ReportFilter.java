package com.example.commerce.report;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 报表查询条件：统计期间（左闭右开）、商户与品类。未使用的条件由报表自行忽略。 */
public record ReportFilter(LocalDateTime from, LocalDateTime until, long tenantId, long categoryId) {
    public ReportFilter {
        if (from == null || until == null || !from.isBefore(until)) {
            throw new IllegalArgumentException("from 必须早于 until，结束日期不包含当天");
        }
        if (tenantId < 1 || categoryId < 1) {
            throw new IllegalArgumentException("商户与品类编号必须为正数");
        }
    }

    public static ReportFilter defaults() {
        return of(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1), 1, 5);
    }

    public static ReportFilter of(LocalDate from, LocalDate until, long tenantId, long categoryId) {
        return new ReportFilter(from.atStartOfDay(), until.atStartOfDay(), tenantId, categoryId);
    }
}
