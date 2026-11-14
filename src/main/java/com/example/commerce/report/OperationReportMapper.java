package com.example.commerce.report;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OperationReportMapper {
    @Select("""
        SELECT o.customer_id,c.name customer_name,COUNT(*) order_count,SUM(o.total_amount) order_value,
          SUM((SELECT COUNT(*) FROM return_order r WHERE r.order_id=o.id)) return_count,
          SUM((SELECT COUNT(*) FROM return_order r WHERE r.order_id=o.id AND r.status='REFUNDED')) refunded_count
        FROM sales_order o JOIN customer c ON c.id=o.customer_id
        WHERE o.status IN ('PAID','SHIPPED','COMPLETED','REFUNDING') AND o.ordered_at>=#{from} AND o.ordered_at<#{until}
        GROUP BY o.customer_id,c.name ORDER BY return_count DESC,o.customer_id LIMIT 50
        """)
    List<Map<String, Object>> customerAfterSales(ReportFilter filter);

    @Select("""
        SELECT o.channel,COUNT(*) line_count,SUM(i.quantity) units,SUM(i.line_amount) revenue
        FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
        WHERE (o.status='PENDING' OR o.id IN (SELECT p.order_id FROM payment p WHERE p.status='REFUNDED'))
          AND i.created_at>=#{from} AND i.created_at<#{until}
        GROUP BY o.channel ORDER BY o.channel
        """)
    List<Map<String, Object>> paymentExceptionSales(ReportFilter filter);

    @Select("""
        SELECT w.name warehouse_name,COUNT(*) order_count,SUM(o.total_amount) order_value,
          SUM((SELECT COUNT(*) FROM shipment s WHERE s.order_id=o.id)) shipment_count,
          SUM((SELECT COUNT(*) FROM shipment s WHERE s.order_id=o.id AND s.status='DELIVERED')) delivered_count
        FROM sales_order o JOIN warehouse w ON w.id=o.warehouse_id
        WHERE o.status IN ('PAID','SHIPPED','COMPLETED','REFUNDING') AND o.ordered_at>=#{from} AND o.ordered_at<#{until}
        GROUP BY w.id,w.name ORDER BY w.id
        """)
    List<Map<String, Object>> warehouseShipmentStats(ReportFilter filter);

    @Select("""
        SELECT o.channel,c.tier,COUNT(*) line_count,SUM(i.quantity) units,SUM(i.line_amount) revenue,
          SUM(i.quantity*s.weight_grams) weight_grams
        FROM sales_order_item i JOIN sales_order o ON CAST(o.id AS CHAR)=CAST(i.order_id AS CHAR)
        JOIN customer c ON c.id=o.customer_id JOIN sku s ON s.id=i.sku_id
        WHERE i.fulfillment_status<>'RETURNED' AND i.created_at>=#{from} AND i.created_at<#{until}
        GROUP BY o.channel,c.tier ORDER BY o.channel,c.tier
        """)
    List<Map<String, Object>> channelTierSales(ReportFilter filter);

    @Select("""
        SELECT o.channel,COUNT(*) line_count,SUM(i.line_amount) revenue,
          SUM((SELECT COUNT(*) FROM shopping_cart_item ci WHERE ci.sku_id=i.sku_id)) cart_mentions
        FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
        WHERE i.fulfillment_status<>'RETURNED' AND i.created_at>=#{from} AND i.created_at<#{until}
        GROUP BY o.channel ORDER BY o.channel
        """)
    List<Map<String, Object>> channelCartInterest(ReportFilter filter);

    @Select("""
        SELECT c.tier,COUNT(*) line_count,SUM(i.line_amount) revenue,
          SUM((SELECT COUNT(*) FROM customer_coupon cc WHERE cc.customer_id=o.customer_id AND cc.status='AVAILABLE')) coupon_reach
        FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN customer c ON c.id=o.customer_id
        WHERE i.created_at>=#{from} AND i.created_at<#{until}
        GROUP BY c.tier ORDER BY c.tier
        """)
    List<Map<String, Object>> tierCouponReach(ReportFilter filter);

    @Select("""
        SELECT w.id warehouse_id,w.name warehouse_name,
          (SELECT SUM(i.quantity) FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
            WHERE o.warehouse_id=w.id AND i.fulfillment_status='PACKED' AND i.created_at>=#{from} AND i.created_at<#{until}) packed_units,
          (SELECT SUM(i.quantity) FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
            WHERE o.warehouse_id=w.id AND i.fulfillment_status='SHIPPED' AND i.created_at>=#{from} AND i.created_at<#{until}) shipped_units,
          (SELECT SUM(i.quantity*s.weight_grams) FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
            JOIN sku s ON s.id=i.sku_id
            WHERE o.warehouse_id=w.id AND i.fulfillment_status IN ('PACKED','SHIPPED')
              AND i.created_at>=#{from} AND i.created_at<#{until}) outbound_weight
        FROM warehouse w ORDER BY w.id
        """)
    List<Map<String, Object>> warehouseFulfillmentStage(ReportFilter filter);

    @Select("""
        SELECT su.id supplier_id,su.name supplier_name,COUNT(*) line_count,SUM(i.line_amount) revenue,
          SUM((SELECT COUNT(*) FROM product_review r WHERE r.sku_id=i.sku_id AND r.rating=5)) five_star_mentions
        FROM sales_order_item i JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id
        JOIN supplier su ON su.id=p.supplier_id
        WHERE i.quantity>=2 AND i.created_at>=#{from} AND i.created_at<#{until}
        GROUP BY su.id,su.name ORDER BY five_star_mentions DESC,su.id LIMIT 50
        """)
    List<Map<String, Object>> supplierFiveStar(ReportFilter filter);

    @Select("""
        SELECT o.channel,c.tier,COUNT(*) first_orders,SUM(o.total_amount) first_order_amount,AVG(o.total_amount) average_amount
        FROM sales_order o JOIN customer c ON c.id=o.customer_id
        WHERE o.status IN ('SHIPPED','COMPLETED') AND o.channel<>'STORE' AND o.ordered_at>=#{from} AND o.ordered_at<#{until}
          AND o.ordered_at=(SELECT MIN(x.ordered_at) FROM sales_order x WHERE x.customer_id=o.customer_id)
        GROUP BY o.channel,c.tier ORDER BY o.channel,c.tier
        """)
    List<Map<String, Object>> firstOrderChannel(ReportFilter filter);

    @Select("""
        SELECT o.channel,SUM(i.discount_amount) discount_amount,SUM(i.unit_price*i.quantity) gross_amount,
          SUM(i.discount_amount)/NULLIF(SUM(i.unit_price*i.quantity),0) discount_rate
        FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
        LEFT JOIN customer c ON c.id=o.customer_id
        LEFT JOIN warehouse w ON w.id=o.warehouse_id
        LEFT JOIN tenant t ON t.id=o.tenant_id
        LEFT JOIN sku s ON s.id=i.sku_id
        LEFT JOIN product p ON p.id=s.product_id
        LEFT JOIN category ca ON ca.id=p.category_id
        LEFT JOIN brand b ON b.id=p.brand_id
        WHERE i.created_at>=#{from} AND i.created_at<#{until}
        GROUP BY o.channel ORDER BY o.channel
        """)
    List<Map<String, Object>> channelDiscountRate(ReportFilter filter);

    @Select("""
        SELECT ca.name category_name,c.tier,COUNT(*) line_count,SUM(i.line_amount) revenue,
          SUM((SELECT COUNT(*) FROM inventory iv WHERE iv.sku_id=i.sku_id AND iv.available_qty>iv.reorder_point)) stocked_locations
        FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN customer c ON c.id=o.customer_id
        JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id JOIN category ca ON ca.id=p.category_id
        WHERE o.status<>'CANCELLED' AND i.created_at>=#{from} AND i.created_at<#{until}
        GROUP BY ca.id,ca.name,c.tier ORDER BY stocked_locations DESC,ca.id,c.tier LIMIT 100
        """)
    List<Map<String, Object>> categoryStockCoverage(ReportFilter filter);
}
