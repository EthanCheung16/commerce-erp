package com.example.commerce.purchase;

import com.example.commerce.common.Periods;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Validated
@RestController
@RequestMapping("/api")
public class PurchaseController {
    private final PurchaseMapper mapper;

    public PurchaseController(PurchaseMapper mapper) { this.mapper = mapper; }

    @GetMapping("/purchases")
    public List<Map<String, Object>> purchases(@RequestParam(required = false) String status,
            @RequestParam(required = false) @Positive Long beforeId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return mapper.purchases(status, beforeId, limit);
    }

    @GetMapping("/purchases/by-no")
    public List<Map<String, Object>> byNo(@RequestParam @Size(min = 4, max = 20) String poNo) {
        return mapper.purchaseByNo(poNo.trim());
    }

    @GetMapping("/purchases/{id}")
    public Map<String, Object> purchase(@PathVariable @Positive long id) {
        var header = mapper.purchase(id);
        if (header == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "记录不存在");
        return Map.of("header", header, "items", mapper.purchaseItems(id));
    }

    @GetMapping("/suppliers/{id}/receipts")
    public List<Map<String, Object>> supplierReceipts(@PathVariable @Positive long id) {
        return mapper.supplierReceipts(id);
    }

    @GetMapping("/suppliers/{id}/sku-sales")
    public List<Map<String, Object>> supplierSkuSales(@PathVariable @Positive long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 366);
        return mapper.supplierSkuSales(id, from, until);
    }

    @GetMapping("/suppliers/{id}/restock-suggestions")
    public List<Map<String, Object>> restock(@PathVariable @Positive long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate asOf) {
        return mapper.restockSuggestions(id, asOf);
    }

    @GetMapping("/warehouses/{id}/receipts")
    public List<Map<String, Object>> warehouseReceipts(@PathVariable @Min(1) @Max(4) long id,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 92);
        return mapper.warehouseReceipts(id, from, until);
    }
}
