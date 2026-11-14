package com.example.commerce.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** 日期区间与分页参数的统一校验。 */
public final class Periods {
    private Periods() {}

    public static void require(LocalDate from, LocalDate until, int maxDays) {
        if (from == null || until == null || !from.isBefore(until)) {
            throw new IllegalArgumentException("开始日期必须早于结束日期，结束日期不包含当天");
        }
        if (ChronoUnit.DAYS.between(from, until) > maxDays) {
            throw new IllegalArgumentException("查询区间不能超过 " + maxDays + " 天");
        }
    }

    public static int offset(int page, int size) {
        return (page - 1) * size;
    }
}
