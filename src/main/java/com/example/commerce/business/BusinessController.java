package com.example.commerce.business;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api")
public class BusinessController {
    private final BusinessMapper mapper;
    private final BusinessService service;
    public BusinessController(BusinessMapper mapper,BusinessService service) {
        this.mapper=mapper; this.service=service;
    }

    @GetMapping("/dashboard")
    public Map<String,Object> dashboard() {
        return Map.of("orders",mapper.dashboardOrders(),"inventory",mapper.dashboardInventory());
    }

    @GetMapping("/options")
    public Map<String,Object> options() {
        return Map.of("categories",mapper.categories(),"warehouses",mapper.warehouses());
    }

    @GetMapping("/products/{id}/skus")
    public List<Map<String,Object>> productSkus(@PathVariable @Positive long id) { return mapper.productSkus(id); }

    @GetMapping("/purchases")
    public List<Map<String,Object>> purchases(@RequestParam(required=false) String status,
            @RequestParam(required=false) @Positive Long beforeId,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int limit) {
        return mapper.purchases(status,beforeId,limit);
    }

    @GetMapping("/returns")
    public List<Map<String,Object>> returns(@RequestParam(required=false) String status,
            @RequestParam(required=false) @Positive Long beforeId,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int limit) {
        return mapper.returns(status,beforeId,limit);
    }

    @GetMapping("/orders")
    public List<Map<String,Object>> orders(@RequestParam(required=false) @Positive Long customerId,
            @RequestParam(required=false) String status,@RequestParam(required=false) @Positive Long beforeId,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int limit) {
        return mapper.orders(customerId,status,beforeId,limit);
    }
    @GetMapping("/orders/{id}")
    public Map<String,Object> order(@PathVariable @Positive long id) { return service.order(id); }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createOrder(@RequestBody @Valid OrderRequest request) { return service.createOrder(request); }

    @GetMapping("/products")
    public List<Map<String,Object>> products(@RequestParam(required=false) @Positive Long categoryId,
            @RequestParam(defaultValue="0") @Min(0) long afterId,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int limit) {
        return mapper.products(categoryId,afterId,limit);
    }

    @GetMapping("/inventory")
    public List<Map<String,Object>> inventory(@RequestParam(defaultValue="1") @Min(1) @Max(4) long warehouseId,
            @RequestParam(defaultValue="0") @Min(0) long afterSkuId,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int limit) {
        return mapper.inventory(warehouseId,afterSkuId,limit);
    }

    @GetMapping("/purchases/{id}")
    public Map<String,Object> purchase(@PathVariable @Positive long id) { return service.purchase(id); }

    @GetMapping("/returns/{id}")
    public Map<String,Object> returnOrder(@PathVariable @Positive long id) { return service.returnOrder(id); }

    @GetMapping("/health")
    public Map<String,String> health() { return Map.of("status","UP","application","commerce-erp"); }
}
