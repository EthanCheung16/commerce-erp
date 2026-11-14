package com.example.commerce.finance;

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
public class FinanceController {
    private static final String BATCH = "ST\\d{8}";
    private final FinanceMapper mapper;

    public FinanceController(FinanceMapper mapper) { this.mapper = mapper; }

    @GetMapping("/payments/by-transaction-no")
    public List<Map<String, Object>> byTransactionNo(@RequestParam @Size(min = 6, max = 64) String transactionNo) {
        return mapper.paymentByTransactionNo(transactionNo.trim());
    }

    @GetMapping("/payments/daily")
    public List<Map<String, Object>> daily(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate day) {
        return mapper.dailyPayments(day);
    }

    @GetMapping("/payments/paid-orders")
    public List<Map<String, Object>> paidOrders(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate day) {
        return mapper.ordersByPaidDate(day);
    }

    @GetMapping("/payments/pages")
    public List<Map<String, Object>> page(@RequestParam(defaultValue = "1") @Min(1) @Max(5000) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        return mapper.paymentPage(Periods.offset(page, size), size);
    }

    @GetMapping("/payments/search")
    public List<Map<String, Object>> search(@RequestParam @Size(min = 4, max = 40) String keyword) {
        return mapper.paymentSearch(keyword.trim());
    }

    @GetMapping("/payments/order-check")
    public List<Map<String, Object>> orderCheck(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 366);
        return mapper.paymentCheck(from, until);
    }

    @GetMapping("/payments/ledger")
    public List<Map<String, Object>> ledger(@RequestParam @Pattern(regexp = "ALIPAY|WECHAT|CARD|BANK") String method,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 31);
        return mapper.paymentLedger(method, from, until);
    }

    @GetMapping("/payments/calendar")
    public List<Map<String, Object>> calendar(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 62);
        return mapper.calendar(from, until);
    }

    @GetMapping("/settlements")
    public List<Map<String, Object>> settlements(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate until) {
        Periods.require(from, until, 31);
        return mapper.settlements(from, until);
    }

    @GetMapping("/settlements/summary")
    public List<Map<String, Object>> summary(@RequestParam @Pattern(regexp = BATCH) String fromBatch,
            @RequestParam @Pattern(regexp = BATCH) String toBatch) {
        return mapper.settlementSummary(fromBatch, toBatch);
    }

    @GetMapping("/settlements/differences")
    public List<Map<String, Object>> differences(@RequestParam @Pattern(regexp = BATCH) String fromBatch,
            @RequestParam @Pattern(regexp = BATCH) String toBatch) {
        return mapper.settlementDifferences(fromBatch, toBatch);
    }

    @GetMapping("/settlements/{batchNo}")
    public List<Map<String, Object>> detail(@PathVariable @Pattern(regexp = BATCH) String batchNo) {
        return mapper.settlementDetail(batchNo);
    }
}
