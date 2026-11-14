package com.example.commerce.logistics;

import com.example.commerce.common.Periods;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/shipments")
public class LogisticsController {
    private final LogisticsMapper mapper;

    public LogisticsController(LogisticsMapper mapper) { this.mapper = mapper; }

    @GetMapping("/by-tracking-no")
    public List<Map<String, Object>> byTrackingNo(@RequestParam @Size(min = 6, max = 48) String trackingNo) {
        return mapper.byTrackingNo(trackingNo.trim());
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam @Size(min = 4, max = 40) String keyword) {
        return mapper.search(keyword.trim());
    }

    @GetMapping("/pages")
    public List<Map<String, Object>> page(@RequestParam(defaultValue = "1") @Min(1) @Max(5000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return mapper.shipmentPage(Periods.offset(page, size), size);
    }

    @GetMapping("/by-customer")
    public List<Map<String, Object>> byCustomer(@RequestParam @Positive long customerId) {
        return mapper.customerShipments(customerId);
    }

    @GetMapping("/tracking")
    public List<Map<String, Object>> tracking(@RequestParam @Positive long customerId) {
        return mapper.customerTracking(customerId);
    }

    @GetMapping("/overdue")
    public List<Map<String, Object>> overdue(@RequestParam(defaultValue = "150") @Min(24) @Max(2000) int hours) {
        return mapper.overdueDeliveries(hours);
    }

    @GetMapping("/daily")
    public List<Map<String, Object>> daily(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 62);
        return mapper.daily(from, until);
    }
}
