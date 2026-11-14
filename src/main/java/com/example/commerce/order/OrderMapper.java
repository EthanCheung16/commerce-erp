package com.example.commerce.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderMapper {
    List<Map<String, Object>> orders(@Param("customerId") Long customerId, @Param("status") String status,
            @Param("beforeId") Long beforeId, @Param("limit") int limit);
    Map<String, Object> order(long id);
    List<Map<String, Object>> orderItems(long orderId);

    List<Map<String, Object>> orderByNo(@Param("orderNo") String orderNo);
    List<Map<String, Object>> searchBySuffix(@Param("suffix") String suffix);
    List<Map<String, Object>> searchByRemark(@Param("keyword") String keyword);
    List<Map<String, Object>> quickSearch(@Param("keyword") String keyword);
    List<Map<String, Object>> advancedSearch(@Param("keyword") String keyword);
    List<Map<String, Object>> ordersByProductName(@Param("keyword") String keyword);
    List<Map<String, Object>> orderPage(@Param("offset") int offset, @Param("size") int size);
    List<Map<String, Object>> orderPageByAmount(@Param("offset") int offset, @Param("size") int size);
    Map<String, Object> countOrders(@Param("from") LocalDate from, @Param("until") LocalDate until);
    List<Map<String, Object>> exportOrders(@Param("from") LocalDate from, @Param("until") LocalDate until);
    List<Map<String, Object>> duplicateOrders(@Param("from") LocalDate from, @Param("until") LocalDate until,
            @Param("hours") int hours);
    List<Map<String, Object>> amountCheck(@Param("customerId") long customerId);
    List<Map<String, Object>> promotionOrders(@Param("code") String code, @Param("from") LocalDate from,
            @Param("until") LocalDate until);
    List<Map<String, Object>> itemPage(@Param("offset") int offset, @Param("size") int size);
    List<Map<String, Object>> orderCalendar(@Param("from") LocalDate from, @Param("until") LocalDate until);
    List<Map<String, Object>> customerMargins(@Param("customerId") long customerId);
    List<Map<String, Object>> amountAnomalies(@Param("from") LocalDate from, @Param("until") LocalDate until,
            @Param("ratio") BigDecimal ratio);
    List<Map<String, Object>> tenantLedger(@Param("tenantId") long tenantId, @Param("from") LocalDate from,
            @Param("until") LocalDate until);

    @Select("SELECT id, tenant_id FROM customer WHERE id=#{id}")
    Map<String, Object> customer(long id);

    @Select("SELECT COUNT(*) FROM customer_address WHERE id=#{addressId} AND customer_id=#{customerId}")
    int ownsAddress(@Param("customerId") long customerId, @Param("addressId") long addressId);

    @Select("SELECT s.id,s.price FROM sku s JOIN product p ON p.id=s.product_id WHERE s.id=#{id} AND p.status='ACTIVE'")
    Map<String, Object> activeSku(long id);

    @Update("UPDATE inventory SET available_qty=available_qty-#{quantity}, reserved_qty=reserved_qty+#{quantity}, updated_at=NOW() WHERE warehouse_id=#{warehouseId} AND sku_id=#{skuId} AND available_qty>=#{quantity}")
    int reserve(@Param("warehouseId") long warehouseId, @Param("skuId") long skuId, @Param("quantity") int quantity);

    @Insert("""
        INSERT INTO sales_order (order_no,tenant_id,customer_id,address_id,warehouse_id,status,channel,
          ordered_at,total_amount,discount_amount,shipping_fee,remark)
        VALUES (#{orderNo},#{tenantId},#{customerId},#{addressId},#{warehouseId},'PENDING',#{channel},
          #{orderedAt},#{totalAmount},0,0,'本地业务接口创建')
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(OrderDraft draft);

    @Insert("""
        INSERT INTO sales_order_item (order_id,sku_id,quantity,unit_price,discount_amount,line_amount,
          created_at,fulfillment_status)
        VALUES (#{orderId},#{skuId},#{quantity},#{price},0,#{amount},#{createdAt},'ALLOCATED')
        """)
    int insertItem(@Param("orderId") long orderId, @Param("skuId") long skuId, @Param("quantity") int quantity,
            @Param("price") BigDecimal price, @Param("amount") BigDecimal amount,
            @Param("createdAt") LocalDateTime createdAt);
}
