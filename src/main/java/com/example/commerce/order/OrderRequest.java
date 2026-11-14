package com.example.commerce.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record OrderRequest(@Positive long customerId, @Positive long addressId,
        @Min(1) @Max(4) long warehouseId,
        @NotBlank @Pattern(regexp = "WEB|APP|MARKETPLACE|STORE") String channel,
        @NotEmpty @Size(max = 20) List<@Valid Line> items) {
    public record Line(@Positive long skuId, @Min(1) @Max(100) int quantity) {}
}
