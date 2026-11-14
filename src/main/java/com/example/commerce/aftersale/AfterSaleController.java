package com.example.commerce.aftersale;

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
@RequestMapping("/api/returns")
public class AfterSaleController {
    private final AfterSaleMapper mapper;

    public AfterSaleController(AfterSaleMapper mapper) { this.mapper = mapper; }

    @GetMapping
    public List<Map<String, Object>> returns(@RequestParam(required = false) String status,
            @RequestParam(required = false) @Positive Long beforeId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return mapper.returns(status, beforeId, limit);
    }

    @GetMapping("/by-no")
    public List<Map<String, Object>> byNo(@RequestParam @Size(min = 4, max = 20) String returnNo) {
        return mapper.returnByNo(returnNo.trim());
    }

    @GetMapping("/{id}")
    public Map<String, Object> returnOrder(@PathVariable @Positive long id) {
        var header = mapper.returnOrder(id);
        if (header == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "记录不存在");
        return Map.of("header", header, "items", mapper.returnItems(id));
    }

    @GetMapping("/restock-check")
    public List<Map<String, Object>> restockCheck(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 31);
        return mapper.restockCheck(from, until);
    }

    @GetMapping("/refund-check")
    public List<Map<String, Object>> refundCheck(@RequestParam @Min(1) @Max(8) long tenantId) {
        return mapper.refundCheck(tenantId);
    }

    @GetMapping("/movements")
    public List<Map<String, Object>> movements(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 92);
        return mapper.returnMovements(from, until);
    }
}
