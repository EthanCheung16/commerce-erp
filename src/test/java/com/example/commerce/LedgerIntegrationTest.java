package com.example.commerce;

import com.example.commerce.finance.FinanceMapper;
import com.example.commerce.inventory.InventoryMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class LedgerIntegrationTest {
    @Autowired FinanceMapper finance;
    @Autowired InventoryMapper inventory;
    @Autowired JdbcTemplate jdbc;

    @Test void receiptsIncludeTimestampPeersAndExcludeOtherMethodsAndDates() {
        payment(910001, "2030-01-01 12:00:00", "1000", "BANK", "SUCCESS");
        payment(910002, "2030-01-02 09:00:00", "10", "BANK", "SUCCESS");
        payment(910003, "2030-01-02 09:00:00", "20", "BANK", "SUCCESS");
        payment(910004, "2030-01-02 10:00:00", "7", "BANK", "SUCCESS");
        payment(910005, "2030-01-03 08:00:00", "5", "BANK", "SUCCESS");
        payment(910006, "2030-01-04 00:00:00", "99", "BANK", "SUCCESS");
        payment(910007, "2030-01-02 09:00:00", "50", "CARD", "SUCCESS");
        payment(910008, "2030-01-02 09:00:00", "40", "BANK", "REFUNDED");

        var rows = finance.paymentLedger("BANK", LocalDate.of(2030, 1, 2), LocalDate.of(2030, 1, 4));
        assertThat(rows).extracting(row -> ((Number) row.get("id")).longValue())
                .containsExactly(910002L, 910003L, 910004L, 910005L);
        assertThat(rows).extracting(row -> ((Number) row.get("running_amount")).intValue())
                .containsExactly(30, 30, 37, 42);
        assertThat(finance.paymentLedger("ALIPAY", LocalDate.of(2030, 1, 2), LocalDate.of(2030, 1, 4))).isEmpty();
    }

    @Test void inventoryUsesIdOrderForTimestampPeersAndCarriesDailyBalances() {
        movement(920001, "2030-01-01 12:00:00", 1000, 1);
        movement(920002, "2030-01-02 09:00:00", 10, 1);
        movement(920003, "2030-01-02 09:00:00", -3, 1);
        movement(920004, "2030-01-03 08:00:00", 7, 1);
        movement(920005, "2030-01-04 00:00:00", 99, 1);
        movement(920006, "2030-01-02 09:00:00", 50, 4);

        var rows = inventory.ledger(1, LocalDate.of(2030, 1, 2), LocalDate.of(2030, 1, 4));
        assertThat(rows).extracting(row -> ((Number) row.get("id")).longValue())
                .containsExactly(920002L, 920003L, 920004L);
        assertThat(rows).extracting(row -> ((Number) row.get("running_balance")).intValue())
                .containsExactly(10, 7, 14);
        assertThat(inventory.ledger(2, LocalDate.of(2030, 1, 2), LocalDate.of(2030, 1, 4))).isEmpty();
    }

    @Test void settlementDifferencesKeepBatchBoundsAndExcludeEqualAndUnmatchedAmounts() {
        var order = jdbc.queryForMap("SELECT platform_order_no,order_no,total_amount FROM sales_order WHERE platform_order_no IS NOT NULL ORDER BY id LIMIT 1");
        long platformNo = Long.parseLong((String) order.get("platform_order_no"));
        var amount = (BigDecimal) order.get("total_amount");
        settlement(930001, "ST20300101", platformNo, amount.subtract(BigDecimal.ONE));
        settlement(930002, "ST20300102", platformNo, amount.subtract(BigDecimal.ONE));
        settlement(930003, "ST20300103", platformNo, amount.add(BigDecimal.ONE));
        settlement(930004, "ST20300104", platformNo, amount.subtract(BigDecimal.ONE));
        settlement(930005, "ST20300102", platformNo, amount);
        settlement(930006, "ST20300102", 99999999999999999L, BigDecimal.ONE);

        var rows = finance.settlementDifferences("ST20300102", "ST20300103");
        assertThat(rows).extracting(row -> ((Number) row.get("id")).longValue()).containsExactly(930002L, 930003L);
        assertThat(rows).allSatisfy(row -> {
            assertThat(row.get("order_no")).isEqualTo(order.get("order_no"));
            assertThat((BigDecimal) row.get("order_amount")).isEqualByComparingTo(amount);
        });
        assertThat(finance.settlementDifferences("ST20300201", "ST20300202")).isEmpty();
    }

    private void payment(long id, String at, String amount, String method, String status) {
        jdbc.update("INSERT INTO payment(id,order_id,method,status,amount,paid_at,transaction_no) VALUES (?,1,?,?,?,?,?)",
                id, method, status, new BigDecimal(amount), at, "LEDGER-" + id);
    }

    private void movement(long id, String at, int quantity, long warehouse) {
        jdbc.update("INSERT INTO stock_movement(id,warehouse_id,sku_id,movement_type,quantity,occurred_at,reference_no) VALUES (?,?,1,'ADJUSTMENT',?,?,?)",
                id, warehouse, quantity, at, "LEDGER-" + id);
    }

    private void settlement(long id, String batch, long platformNo, BigDecimal amount) {
        jdbc.update("INSERT INTO marketplace_settlement(id,batch_no,platform_order_no,settled_amount,commission,settled_at) VALUES (?,?,?,?,0,'2030-01-02')",
                id, batch, platformNo, amount);
    }
}
