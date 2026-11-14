package com.example.commerce.report;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) { this.service = service; }

    @GetMapping
    public List<ReportDefinition> catalog() { return service.catalog(); }

    @GetMapping("/{code}")
    public List<Map<String, Object>> run(@PathVariable String code,
            @RequestParam(defaultValue = "2025-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(defaultValue = "2026-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until,
            @RequestParam(defaultValue = "1") long tenantId,
            @RequestParam(defaultValue = "5") long categoryId) {
        return service.run(code, ReportFilter.of(from, until, tenantId, categoryId));
    }
}
