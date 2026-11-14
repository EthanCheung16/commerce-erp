package com.example.commerce.inventory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InventoryMapper {
    @Select("SELECT id,name,city FROM warehouse ORDER BY id")
    List<Map<String, Object>> warehouses();

    @Select("""
        SELECT iv.sku_id,s.code,s.price,iv.available_qty,iv.reserved_qty,iv.reorder_point
        FROM inventory iv JOIN sku s ON s.id=iv.sku_id
        WHERE iv.warehouse_id=#{warehouseId} AND iv.sku_id>#{afterSkuId}
        ORDER BY iv.sku_id LIMIT #{limit}
        """)
    List<Map<String, Object>> inventory(@Param("warehouseId") long warehouseId,
            @Param("afterSkuId") long afterSkuId, @Param("limit") int limit);

    @Select("""
        SELECT iv.warehouse_id,w.name warehouse_name,s.code sku_code,iv.available_qty,iv.reserved_qty,iv.reorder_point,
          iv.updated_at
        FROM sku s JOIN inventory iv ON iv.sku_id=s.id JOIN warehouse w ON w.id=iv.warehouse_id
        WHERE s.code=#{code} ORDER BY iv.warehouse_id
        """)
    List<Map<String, Object>> stockBySku(@Param("code") String code);

    @Select("""
        SELECT i.id item_id,i.order_id,o.order_no,o.customer_id,s.code sku_code,p.name product_name,i.quantity,
          i.unit_price,i.line_amount,i.fulfillment_status,i.created_at
        FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id
        JOIN product p ON p.id=s.product_id
        WHERE DATE(i.created_at)=#{day} AND o.warehouse_id=#{warehouseId}
        ORDER BY i.created_at,i.id
        """)
    List<Map<String, Object>> dailyItems(@Param("warehouseId") long warehouseId, @Param("day") LocalDate day);

    @Select("""
        SELECT s.id sku_id,s.code sku_code,p.name product_name,iv.available_qty
        FROM inventory iv JOIN sku s ON s.id=iv.sku_id JOIN product p ON p.id=s.product_id
        WHERE iv.warehouse_id=#{warehouseId} AND iv.available_qty>0
          AND s.id NOT IN (SELECT i.sku_id FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
                           WHERE o.warehouse_id=#{warehouseId} AND i.created_at>=#{since})
        ORDER BY iv.available_qty DESC,s.id LIMIT 100
        """)
    List<Map<String, Object>> stagnantSkus(@Param("warehouseId") long warehouseId, @Param("since") LocalDate since);

    @Select("""
        SELECT iv.sku_id,s.code sku_code,iv.available_qty,COALESCE(mv.net_qty,0) movement_net,
          iv.available_qty-COALESCE(mv.net_qty,0) gap
        FROM inventory iv JOIN sku s ON s.id=iv.sku_id
        LEFT JOIN (SELECT warehouse_id,sku_id,SUM(quantity) net_qty FROM stock_movement GROUP BY warehouse_id,sku_id) mv
          ON mv.warehouse_id=iv.warehouse_id AND mv.sku_id=iv.sku_id
        WHERE iv.warehouse_id=#{warehouseId}
        ORDER BY ABS(iv.available_qty-COALESCE(mv.net_qty,0)) DESC,iv.sku_id LIMIT 100
        """)
    List<Map<String, Object>> reconciliation(@Param("warehouseId") long warehouseId);

    @Select("""
        WITH RECURSIVE days(d) AS (
          SELECT DATE(#{from}) UNION ALL SELECT d+INTERVAL 1 DAY FROM days WHERE d+INTERVAL 1 DAY<DATE(#{until}))
        SELECT d.d day,
          (SELECT COUNT(*) FROM sales_order o WHERE o.warehouse_id=#{warehouseId}
            AND DATE_FORMAT(o.ordered_at,'%Y-%m-%d')=DATE_FORMAT(d.d,'%Y-%m-%d')) order_count,
          (SELECT COALESCE(-SUM(m.quantity),0) FROM stock_movement m WHERE m.warehouse_id=#{warehouseId}
            AND m.movement_type='SALE' AND DATE(m.occurred_at)=d.d) outbound_units,
          (SELECT COALESCE(SUM(m.quantity),0) FROM stock_movement m WHERE m.warehouse_id=#{warehouseId}
            AND m.movement_type='PURCHASE' AND DATE(m.occurred_at)=d.d) inbound_units
        FROM days d ORDER BY d.d
        """)
    List<Map<String, Object>> daily(@Param("warehouseId") long warehouseId, @Param("from") LocalDate from,
            @Param("until") LocalDate until);

    @Select("""
        WITH daily_movements AS (
            SELECT m.warehouse_id,DATE(m.occurred_at) movement_day,SUM(m.quantity) quantity
            FROM stock_movement m LEFT JOIN sku s ON s.id=m.sku_id LEFT JOIN product p ON p.id=s.product_id
            GROUP BY m.warehouse_id,DATE(m.occurred_at)
            HAVING MIN(m.occurred_at)>=#{from}
        ), period_movements AS (
            SELECT m.id,m.warehouse_id,m.occurred_at,m.movement_type,m.sku_id,m.quantity,m.reference_no,
              SUM(m.quantity) OVER (PARTITION BY m.warehouse_id,DATE(m.occurred_at)
                ORDER BY m.occurred_at,m.id ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) day_quantity
            FROM stock_movement m
            WHERE m.occurred_at>=#{from} AND m.occurred_at<#{until}
        )
        SELECT m.id,m.occurred_at,m.movement_type,m.sku_id,m.quantity,m.reference_no,
          m.day_quantity+COALESCE((SELECT SUM(d.quantity) FROM daily_movements d
            WHERE d.warehouse_id=m.warehouse_id
              AND d.movement_day<DATE(m.occurred_at)),0) running_balance
        FROM period_movements m WHERE m.warehouse_id=#{warehouseId}
        ORDER BY m.occurred_at,m.id
        """)
    List<Map<String, Object>> ledger(@Param("warehouseId") long warehouseId, @Param("from") LocalDate from,
            @Param("until") LocalDate until);

    @Select("""
        SELECT i.sku_id,MAX(s.code) sku_code,MAX(p.name) product_name,SUM(i.quantity) units,SUM(i.line_amount) revenue
        FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id
        JOIN product p ON p.id=s.product_id
        WHERE i.created_at>=#{from} AND i.created_at<#{until}
        GROUP BY o.warehouse_id,i.sku_id HAVING o.warehouse_id=#{warehouseId}
        ORDER BY units DESC,i.sku_id LIMIT 50
        """)
    List<Map<String, Object>> topSellers(@Param("warehouseId") long warehouseId, @Param("from") LocalDate from,
            @Param("until") LocalDate until);

    @Select("""
        SELECT o.id,o.order_no,o.customer_id,o.ordered_at,o.paid_at,o.total_amount
        FROM sales_order o
        WHERE o.warehouse_id=#{warehouseId} AND o.status='PAID' AND o.paid_at>=#{from} AND o.paid_at<#{until}
          AND (SELECT COUNT(*) FROM stock_movement m WHERE m.reference_no=o.order_no)=0
        ORDER BY o.paid_at,o.id
        """)
    List<Map<String, Object>> pendingOutbound(@Param("warehouseId") long warehouseId, @Param("from") LocalDate from,
            @Param("until") LocalDate until);

    @Select("""
        SELECT o.id,o.order_no,o.customer_id,o.ordered_at,o.total_amount,
          (SELECT SUM(i.quantity) FROM sales_order_item i WHERE i.order_id=o.id) ordered_units,
          (SELECT COALESCE(-SUM(m.quantity),0) FROM stock_movement m WHERE m.reference_no=o.order_no) shipped_units
        FROM sales_order o
        WHERE o.warehouse_id=#{warehouseId} AND o.status='SHIPPED' AND o.ordered_at>=#{from} AND o.ordered_at<#{until}
        ORDER BY o.ordered_at,o.id
        """)
    List<Map<String, Object>> shippedOutboundCheck(@Param("warehouseId") long warehouseId,
            @Param("from") LocalDate from, @Param("until") LocalDate until);
}
