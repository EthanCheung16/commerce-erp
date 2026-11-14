package com.example.commerce.customer;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Validated
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerMapper mapper;

    public CustomerController(CustomerMapper mapper) { this.mapper = mapper; }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam @Size(min = 2, max = 40) String keyword) {
        return mapper.search(keyword.trim());
    }

    @GetMapping("/by-phone/latest-order")
    public List<Map<String, Object>> latestOrder(@RequestParam @Pattern(regexp = "\\d{11}") String phone) {
        return mapper.latestOrderByPhone(phone);
    }

    @GetMapping("/upgrade-candidates")
    public List<Map<String, Object>> upgradeCandidates(@RequestParam @Min(2020) @Max(2100) int year,
            @RequestParam @DecimalMin("0") BigDecimal threshold) {
        return mapper.upgradeCandidates(year, threshold);
    }

    @GetMapping("/segments")
    public List<Map<String, Object>> segments(@RequestParam @Min(1) @Max(8) long tenantId) {
        return mapper.segments(tenantId);
    }

    @GetMapping("/{id}/overview")
    public Map<String, Object> overview(@PathVariable @Positive long id) {
        return found(mapper.overview(id));
    }

    @GetMapping("/{id}/addresses")
    public List<Map<String, Object>> addresses(@PathVariable @Positive long id) { return mapper.addresses(id); }

    @GetMapping("/{id}/ranking")
    public Map<String, Object> ranking(@PathVariable @Positive long id) {
        return found(mapper.ranking(id));
    }

    @GetMapping("/{id}/timeline")
    public List<Map<String, Object>> timeline(@PathVariable @Positive long id) { return mapper.timeline(id); }

    @GetMapping("/{id}/outbound")
    public List<Map<String, Object>> outbound(@PathVariable @Positive long id) { return mapper.outboundRecords(id); }

    @GetMapping("/{id}/related-orders")
    public List<Map<String, Object>> relatedOrders(@PathVariable @Positive long id) { return mapper.relatedOrders(id); }

    @GetMapping("/{id}/fulfillment")
    public List<Map<String, Object>> fulfillment(@PathVariable @Positive long id) { return mapper.orderFulfillment(id); }

    @GetMapping("/{id}/frequent-skus")
    public List<Map<String, Object>> frequentSkus(@PathVariable @Positive long id) { return mapper.frequentSkus(id); }

    private static Map<String, Object> found(Map<String, Object> row) {
        if (row == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "客户不存在或暂无订单");
        return row;
    }
}
