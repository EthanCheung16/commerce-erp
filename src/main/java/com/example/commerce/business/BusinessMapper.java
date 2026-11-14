package com.example.commerce.business;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.*;

@Mapper
public interface BusinessMapper {
    Map<String, Object> dashboardOrders();
    Map<String, Object> dashboardInventory();
    List<Map<String, Object>> purchases(@Param("status") String status, @Param("beforeId") Long beforeId,
            @Param("limit") int limit);
    List<Map<String, Object>> returns(@Param("status") String status, @Param("beforeId") Long beforeId,
            @Param("limit") int limit);

    @Select("SELECT id,name FROM category ORDER BY id")
    List<Map<String, Object>> categories();

    @Select("SELECT id,name,city FROM warehouse ORDER BY id")
    List<Map<String, Object>> warehouses();

    @Select("SELECT id,code,specification,price,weight_grams FROM sku WHERE product_id=#{productId} ORDER BY id LIMIT 100")
    List<Map<String, Object>> productSkus(long productId);

    List<Map<String, Object>> orders(@Param("customerId") Long customerId, @Param("status") String status,
            @Param("beforeId") Long beforeId, @Param("limit") int limit);
    Map<String, Object> order(long id);
    List<Map<String, Object>> orderItems(long orderId);
    List<Map<String, Object>> products(@Param("categoryId") Long categoryId, @Param("afterId") long afterId,
            @Param("limit") int limit);
    List<Map<String, Object>> inventory(@Param("warehouseId") long warehouseId,
            @Param("afterSkuId") long afterSkuId, @Param("limit") int limit);
    Map<String, Object> purchase(long id);
    List<Map<String, Object>> purchaseItems(long id);
    Map<String, Object> returnOrder(long id);
    List<Map<String, Object>> returnItems(long id);

    @Select("SELECT id, tenant_id FROM customer WHERE id=#{id}")
    Map<String, Object> customer(long id);

    @Select("SELECT COUNT(*) FROM customer_address WHERE id=#{addressId} AND customer_id=#{customerId}")
    int ownsAddress(@Param("customerId") long customerId, @Param("addressId") long addressId);

    @Select("SELECT s.id,s.price FROM sku s JOIN product p ON p.id=s.product_id WHERE s.id=#{id} AND p.status='ACTIVE'")
    Map<String, Object> activeSku(long id);

    @Update("UPDATE inventory SET available_qty=available_qty-#{quantity}, reserved_qty=reserved_qty+#{quantity}, updated_at=NOW() WHERE warehouse_id=#{warehouseId} AND sku_id=#{skuId} AND available_qty>=#{quantity}")
    int reserve(@Param("warehouseId") long warehouseId,@Param("skuId") long skuId,@Param("quantity") int quantity);

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
    int insertItem(@Param("orderId") long orderId,@Param("skuId") long skuId,@Param("quantity") int quantity,
            @Param("price") BigDecimal price,@Param("amount") BigDecimal amount,@Param("createdAt") LocalDateTime createdAt);
}
