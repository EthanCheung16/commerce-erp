package com.example.commerce.order;

import com.example.commerce.common.Periods;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderMapper mapper;
    private final OrderService service;

    public OrderController(OrderMapper mapper, OrderService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> orders(@RequestParam(required = false) @Positive Long customerId,
            @RequestParam(required = false) String status, @RequestParam(required = false) @Positive Long beforeId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return mapper.orders(customerId, status, beforeId, limit);
    }

    @GetMapping("/{id}")
    public Map<String, Object> order(@PathVariable @Positive long id) { return service.order(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createOrder(@RequestBody @Valid OrderRequest request) {
        return service.createOrder(request);
    }

    @GetMapping("/search")
    public List<Map<String, Object>> quickSearch(@RequestParam @Size(min = 2, max = 40) String keyword) {
        return mapper.quickSearch(keyword.trim());
    }

    @GetMapping("/by-no")
    public List<Map<String, Object>> byNo(@RequestParam @Size(min = 6, max = 40) String orderNo) {
        return mapper.orderByNo(orderNo.trim());
    }

    @GetMapping("/search/suffix")
    public List<Map<String, Object>> searchBySuffix(@RequestParam @Pattern(regexp = "\\d{4,12}") String suffix) {
        return mapper.searchBySuffix(suffix);
    }

    @GetMapping("/search/remark")
    public List<Map<String, Object>> searchByRemark(@RequestParam @Size(min = 2, max = 40) String keyword) {
        return mapper.searchByRemark(keyword.trim());
    }

    @GetMapping("/search/advanced")
    public List<Map<String, Object>> advancedSearch(@RequestParam @Size(min = 2, max = 40) String keyword) {
        return mapper.advancedSearch(keyword.trim());
    }

    @GetMapping("/by-product")
    public List<Map<String, Object>> byProduct(@RequestParam @Size(min = 1, max = 40) String keyword) {
        return mapper.ordersByProductName(keyword.trim());
    }

    @GetMapping("/pages")
    public List<Map<String, Object>> page(@RequestParam(defaultValue = "1") @Min(1) @Max(10000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return mapper.orderPage(Periods.offset(page, size), size);
    }

    @GetMapping("/pages/by-amount")
    public List<Map<String, Object>> pageByAmount(@RequestParam(defaultValue = "1") @Min(1) @Max(10000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return mapper.orderPageByAmount(Periods.offset(page, size), size);
    }

    @GetMapping("/items/pages")
    public List<Map<String, Object>> itemPage(@RequestParam(defaultValue = "1") @Min(1) @Max(20000) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        return mapper.itemPage(Periods.offset(page, size), size);
    }

    @GetMapping("/count")
    public Map<String, Object> count(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 366);
        return mapper.countOrders(from, until);
    }

    @GetMapping("/export")
    public List<Map<String, Object>> export(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 31);
        return mapper.exportOrders(from, until);
    }

    @GetMapping("/duplicates")
    public List<Map<String, Object>> duplicates(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until,
            @RequestParam(defaultValue = "24") @Min(1) @Max(168) int hours) {
        Periods.require(from, until, 31);
        return mapper.duplicateOrders(from, until, hours);
    }

    @GetMapping("/amount-check")
    public List<Map<String, Object>> amountCheck(@RequestParam @Positive long customerId) {
        return mapper.amountCheck(customerId);
    }

    @GetMapping("/margins")
    public List<Map<String, Object>> margins(@RequestParam @Positive long customerId) {
        return mapper.customerMargins(customerId);
    }

    @GetMapping("/promotion")
    public List<Map<String, Object>> promotionOrders(@RequestParam @NotBlank String code,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 366);
        return mapper.promotionOrders(code.trim(), from, until);
    }

    @GetMapping("/calendar")
    public List<Map<String, Object>> calendar(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 62);
        return mapper.orderCalendar(from, until);
    }

    @GetMapping("/anomalies")
    public List<Map<String, Object>> anomalies(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until,
            @RequestParam(defaultValue = "2") @DecimalMin("1.1") @DecimalMax("10") BigDecimal ratio) {
        Periods.require(from, until, 92);
        return mapper.amountAnomalies(from, until, ratio);
    }

    @GetMapping("/tenant-ledger")
    public List<Map<String, Object>> tenantLedger(@RequestParam @Min(1) @Max(8) long tenantId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 31);
        return mapper.tenantLedger(tenantId, from, until);
    }
}
