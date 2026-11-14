package com.example.commerce.catalog;

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
public class CatalogController {
    private final CatalogMapper mapper;

    public CatalogController(CatalogMapper mapper) { this.mapper = mapper; }

    @GetMapping("/products")
    public List<Map<String, Object>> products(@RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(defaultValue = "0") @Min(0) long afterId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return mapper.products(categoryId, afterId, limit);
    }

    @GetMapping("/products/{id}/skus")
    public List<Map<String, Object>> productSkus(@PathVariable @Positive long id) { return mapper.productSkus(id); }

    @GetMapping("/products/search")
    public List<Map<String, Object>> search(@RequestParam @Size(min = 1, max = 40) String keyword) {
        return mapper.productSearch(keyword.trim());
    }

    @GetMapping("/products/offline-stock")
    public List<Map<String, Object>> offlineStock() { return mapper.offlineStock(); }

    @GetMapping("/brands/{id}/best-sellers")
    public List<Map<String, Object>> bestSellers(@PathVariable @Positive long id) { return mapper.brandBestSellers(id); }

    @GetMapping("/brands/{id}/latest-prices")
    public List<Map<String, Object>> latestPrices(@PathVariable @Positive long id) { return mapper.brandLatestPrices(id); }

    @GetMapping("/categories/{id}/return-rates")
    public List<Map<String, Object>> returnRates(@PathVariable @Positive long id) { return mapper.categoryReturnRates(id); }

    @GetMapping("/categories/{id}/first-buyers")
    public List<Map<String, Object>> firstBuyers(@PathVariable @Positive long id) { return mapper.categoryFirstBuyers(id); }

    @GetMapping("/promotions")
    public List<Map<String, Object>> promotions(
            @RequestParam(defaultValue = "ACTIVE") @Pattern(regexp = "ACTIVE|ENDED") String status) {
        return mapper.promotions(status);
    }

    @GetMapping("/promotions/{code}/daily")
    public List<Map<String, Object>> promotionDaily(@PathVariable @Size(max = 40) String code,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 62);
        return mapper.promotionDaily(code, from, until);
    }

    @GetMapping("/coupons/thresholds")
    public List<Map<String, Object>> couponThresholds(@RequestParam @Min(1) @Max(8) long tenantId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 366);
        return mapper.couponThresholds(tenantId, from, until);
    }
}
