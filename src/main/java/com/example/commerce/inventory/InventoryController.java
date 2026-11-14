package com.example.commerce.inventory;

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
@RequestMapping("/api")
public class InventoryController {
    private final InventoryMapper mapper;

    public InventoryController(InventoryMapper mapper) { this.mapper = mapper; }

    @GetMapping("/inventory")
    public List<Map<String, Object>> inventory(@RequestParam(defaultValue = "1") @Min(1) @Max(4) long warehouseId,
            @RequestParam(defaultValue = "0") @Min(0) long afterSkuId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return mapper.inventory(warehouseId, afterSkuId, limit);
    }

    @GetMapping("/inventory/by-sku")
    public List<Map<String, Object>> stockBySku(@RequestParam @Size(min = 4, max = 40) String code) {
        return mapper.stockBySku(code.trim());
    }

    @GetMapping("/warehouses/{id}/daily-items")
    public List<Map<String, Object>> dailyItems(@PathVariable @Min(1) @Max(4) long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate day) {
        return mapper.dailyItems(id, day);
    }

    @GetMapping("/warehouses/{id}/stagnant-skus")
    public List<Map<String, Object>> stagnant(@PathVariable @Min(1) @Max(4) long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate since) {
        return mapper.stagnantSkus(id, since);
    }

    @GetMapping("/warehouses/{id}/reconciliation")
    public List<Map<String, Object>> reconciliation(@PathVariable @Min(1) @Max(4) long id) {
        return mapper.reconciliation(id);
    }

    @GetMapping("/warehouses/{id}/daily")
    public List<Map<String, Object>> daily(@PathVariable @Min(1) @Max(4) long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 62);
        return mapper.daily(id, from, until);
    }

    @GetMapping("/warehouses/{id}/ledger")
    public List<Map<String, Object>> ledger(@PathVariable @Min(1) @Max(4) long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 31);
        return mapper.ledger(id, from, until);
    }

    @GetMapping("/warehouses/{id}/top-sellers")
    public List<Map<String, Object>> topSellers(@PathVariable @Min(1) @Max(4) long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 366);
        return mapper.topSellers(id, from, until);
    }

    @GetMapping("/warehouses/{id}/pending-outbound")
    public List<Map<String, Object>> pendingOutbound(@PathVariable @Min(1) @Max(4) long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 31);
        return mapper.pendingOutbound(id, from, until);
    }

    @GetMapping("/warehouses/{id}/shipped-check")
    public List<Map<String, Object>> shippedCheck(@PathVariable @Min(1) @Max(4) long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 31);
        return mapper.shippedOutboundCheck(id, from, until);
    }
}
