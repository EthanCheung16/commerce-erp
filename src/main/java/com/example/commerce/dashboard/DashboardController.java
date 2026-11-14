package com.example.commerce.dashboard;

import com.example.commerce.common.Periods;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardMapper mapper;

    public DashboardController(DashboardMapper mapper) { this.mapper = mapper; }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        var orders = new java.util.LinkedHashMap<String, Object>();
        long total = 0;
        for (var status : List.of("PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED", "REFUNDING")) {
            orders.put(status.toLowerCase() + "_count", 0L);
        }
        for (var row : mapper.orderStatusCounts()) {
            long count = ((Number) row.get("order_count")).longValue();
            orders.put(String.valueOf(row.get("status")).toLowerCase() + "_count", count);
            total += count;
        }
        orders.put("order_count", total);
        orders.put("sales_amount", mapper.receivedAmount());
        return Map.of("orders", orders, "inventory", mapper.inventorySummary());
    }

    @GetMapping("/dashboard/channels")
    public List<Map<String, Object>> channels(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 366);
        return mapper.channelBoard(from, until);
    }

    @GetMapping("/options")
    public Map<String, Object> options() {
        return Map.of("categories", mapper.categories(), "warehouses", mapper.warehouses(), "tenants", mapper.tenants());
    }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "UP", "application", "commerce-erp"); }
}
