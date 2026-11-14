package com.example.commerce.business;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 仅在事务内构建，由 MyBatis 回填数据库生成的主键。 */
public class OrderDraft {
    public Long id;
    public String orderNo;
    public long tenantId;
    public long customerId;
    public long addressId;
    public long warehouseId;
    public String channel;
    public LocalDateTime orderedAt;
    public BigDecimal totalAmount;
}
