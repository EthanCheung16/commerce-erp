package com.example.commerce;

import com.example.commerce.order.*;
import com.example.commerce.report.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectIntegrationTest {
    @Autowired ReportService reports;
    @Autowired OrderService orders;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;

    @Test void reportCatalogIsComplete() throws Exception {
        var catalog = reports.catalog();
        assertThat(catalog).hasSize(30);
        assertThat(catalog).extracting(ReportDefinition::code).doesNotHaveDuplicates();
        assertThat(catalog).extracting(ReportDefinition::statement).doesNotHaveDuplicates();
        assertThat(catalog).allSatisfy(report -> {
            assertThat(report.title()).isNotBlank();
            assertThat(report.category()).isIn("SALES", "CUSTOMERS", "PRODUCTS", "FULFILLMENT", "PROCUREMENT", "FINANCE");
            assertThat(report.parameters()).contains("period").isSubsetOf("period", "tenant", "category");
        });
        mvc.perform(get("/api/reports")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").isNotEmpty())
            .andExpect(jsonPath("$[0].parameters").isArray())
            .andExpect(jsonPath("$[0].statement").doesNotExist());
    }

    @Test void businessReadsAndInvalidInput() throws Exception {
        mvc.perform(get("/api/orders/1")).andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(3));
        mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(20));
        mvc.perform(get("/api/inventory")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(20));
        mvc.perform(get("/api/purchases/1")).andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(3))
            .andExpect(jsonPath("$.header.po_no").value("PO-000001"));
        mvc.perform(get("/api/returns/1")).andExpect(status().isOk()).andExpect(jsonPath("$.header.return_no").value("RT-000001"));
        mvc.perform(get("/api/reports/does-not-exist")).andExpect(status().isNotFound());
        mvc.perform(get("/api/orders").param("limit", "101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reports/monthly-sales").param("from", "2026-02-01")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/orders/calendar").param("from", "2025-01-01").param("until", "2025-06-01"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/orders/search/suffix").param("suffix", "ab")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/settlements/BATCH")).andExpect(status().isBadRequest());
    }

    @Test void frontEndAssetsAndSupportingEndpoints() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("/assets/app.js")));
        for (var asset : List.of("app.css", "app.js", "reports.js", "dialogs.js", "ui.js", "workbench.js")) {
            mvc.perform(get("/assets/" + asset)).andExpect(status().isOk());
        }
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/api/dashboard")).andExpect(status().isOk())
            .andExpect(jsonPath("$.orders.order_count").isNumber())
            .andExpect(jsonPath("$.inventory.available_qty").isNumber());
        mvc.perform(get("/api/options")).andExpect(status().isOk())
            .andExpect(jsonPath("$.warehouses.length()").value(4))
            .andExpect(jsonPath("$.tenants.length()").value(8));
        mvc.perform(get("/api/purchases").param("limit", "21")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(21));
        mvc.perform(get("/api/returns").param("status", "REQUESTED")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("REQUESTED"));
        mvc.perform(get("/api/products/1/skus")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test @Transactional void acceptsFrontEndOrderPayloadAndReturnsUsefulErrors() throws Exception {
        mvc.perform(post("/api/orders").contentType("application/json").content("""
            {"customerId":1,"addressId":1,"warehouseId":1,"channel":"WEB","items":[{"skuId":1,"quantity":2}]}
            """)).andExpect(status().isCreated()).andExpect(jsonPath("$.header.status").value("PENDING"))
            .andExpect(jsonPath("$.items[0].quantity").value(2));
        mvc.perform(post("/api/orders").contentType("application/json").content("""
            {"customerId":1,"addressId":1,"warehouseId":1,"channel":"WEB","items":[]}
            """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test @Transactional void orderCreationReservesStockAndUsesDatabasePrices() {
        int before = quantity(1);
        var result = orders.createOrder(new OrderRequest(1, 1, 1, "WEB", List.of(new OrderRequest.Line(1, 2))));
        var header = (Map<?, ?>) result.get("header");
        assertThat(header.get("status")).isEqualTo("PENDING");
        assertThat(header.get("total_amount"))
            .isEqualTo(jdbc.queryForObject("SELECT price*2 FROM sku WHERE id=1", java.math.BigDecimal.class));
        assertThat(quantity(1)).isEqualTo(before - 2);
        assertThat((List<?>) result.get("items")).hasSize(1);
    }

    @Test void insufficientStockRollsBackEarlierReservations() {
        int before = quantity(1);
        long count = jdbc.queryForObject("SELECT COUNT(*) FROM sales_order", Long.class);
        assertThatThrownBy(() -> orders.createOrder(new OrderRequest(1, 1, 1, "WEB",
            List.of(new OrderRequest.Line(1, 1), new OrderRequest.Line(2, 100)))))
            .isInstanceOf(ResponseStatusException.class);
        assertThat(quantity(1)).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sales_order", Long.class)).isEqualTo(count);
    }

    private int quantity(long skuId) {
        return jdbc.queryForObject("SELECT available_qty FROM inventory WHERE warehouse_id=1 AND sku_id=?", Integer.class, skuId);
    }
}
