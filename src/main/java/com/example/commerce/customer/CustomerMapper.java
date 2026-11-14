package com.example.commerce.customer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CustomerMapper {
    @Select("""
        SELECT c.id,c.name,c.email,c.phone,c.tier,
          (SELECT COUNT(*) FROM sales_order o WHERE o.customer_id=c.id) order_count,
          (SELECT SUM(o.total_amount) FROM sales_order o WHERE o.customer_id=c.id) total_spent
        FROM customer c
        WHERE c.name LIKE CONCAT('%',#{keyword},'%') OR c.email LIKE CONCAT('%',#{keyword},'%')
        ORDER BY total_spent DESC,c.id LIMIT 20
        """)
    List<Map<String, Object>> search(@Param("keyword") String keyword);

    @Select("""
        SELECT c.id,c.name,c.tier,c.province,c.registered_at,s.order_count,s.total_spent,s.last_order_at,
          pay.paid_amount,r.return_count,r.refund_amount
        FROM customer c
        LEFT JOIN (SELECT customer_id,COUNT(*) order_count,SUM(total_amount) total_spent,MAX(ordered_at) last_order_at
                   FROM sales_order GROUP BY customer_id) s ON s.customer_id=c.id
        LEFT JOIN (SELECT o.customer_id,SUM(p.amount) paid_amount FROM payment p JOIN sales_order o ON o.id=p.order_id
                   WHERE p.status='SUCCESS' GROUP BY o.customer_id) pay ON pay.customer_id=c.id
        LEFT JOIN (SELECT customer_id,COUNT(*) return_count,SUM(refund_amount) refund_amount
                   FROM return_order GROUP BY customer_id) r ON r.customer_id=c.id
        WHERE c.id=#{customerId}
        """)
    Map<String, Object> overview(@Param("customerId") long customerId);

    @Select("SELECT id,province,city,street,postal_code FROM customer_address WHERE customer_id=#{customerId} ORDER BY id")
    List<Map<String, Object>> addresses(@Param("customerId") long customerId);

    @Select("""
        SELECT x.id,x.order_no,x.customer_id,x.status,x.ordered_at,x.total_amount,x.warehouse_id
        FROM (SELECT o.id,o.customer_id,o.order_no,o.status,o.ordered_at,o.total_amount,o.warehouse_id,
                ROW_NUMBER() OVER (PARTITION BY o.customer_id ORDER BY o.ordered_at DESC,o.id DESC) rn
              FROM sales_order o) x
        WHERE x.rn=1 AND x.customer_id IN (SELECT c.id FROM customer c WHERE c.phone=#{phone})
        """)
    List<Map<String, Object>> latestOrderByPhone(@Param("phone") String phone);

    @Select("""
        SELECT c.id,c.name,c.tier,c.province,
          (SELECT SUM(o.total_amount) FROM sales_order o WHERE o.customer_id=c.id AND YEAR(o.ordered_at)=#{year}) annual_spent
        FROM customer c WHERE c.tier IN ('BRONZE','SILVER')
        HAVING annual_spent>=#{threshold}
        ORDER BY annual_spent DESC,c.id LIMIT 50
        """)
    List<Map<String, Object>> upgradeCandidates(@Param("year") int year, @Param("threshold") BigDecimal threshold);

    @Select("""
        SELECT 'ORDER' event_type,o.order_no reference_no,o.ordered_at occurred_at,o.total_amount amount
        FROM sales_order o WHERE o.customer_id=#{customerId}
        UNION
        SELECT 'PAYMENT',p.transaction_no,p.paid_at,p.amount
        FROM payment p JOIN sales_order o ON o.id=p.order_id WHERE o.customer_id=#{customerId}
        UNION
        SELECT 'SHIPMENT',s.tracking_no,s.shipped_at,s.freight_cost
        FROM shipment s JOIN sales_order o ON o.id=s.order_id WHERE o.customer_id=#{customerId}
        UNION
        SELECT 'RETURN',r.return_no,r.requested_at,r.refund_amount FROM return_order r WHERE r.customer_id=#{customerId}
        UNION
        SELECT 'STOCK_OUT',m.reference_no,m.occurred_at,m.quantity
        FROM stock_movement m JOIN sales_order o ON o.order_no=m.reference_no
        WHERE m.movement_type='SALE' AND o.customer_id=#{customerId}
        ORDER BY occurred_at DESC,event_type,reference_no LIMIT 100
        """)
    List<Map<String, Object>> timeline(@Param("customerId") long customerId);

    @Select("""
        SELECT * FROM (
          SELECT c.id customer_id,c.name,c.tenant_id,COUNT(DISTINCT o.id) order_count,SUM(i.line_amount) total_spent,
            RANK() OVER (PARTITION BY c.tenant_id ORDER BY SUM(i.line_amount) DESC) tenant_rank,
            RANK() OVER (ORDER BY SUM(i.line_amount) DESC) overall_rank
          FROM customer c JOIN sales_order o ON o.customer_id=c.id JOIN sales_order_item i ON i.order_id=o.id
          WHERE o.status<>'CANCELLED' GROUP BY c.id,c.name,c.tenant_id) x
        WHERE x.customer_id=#{customerId}
        """)
    Map<String, Object> ranking(@Param("customerId") long customerId);

    @Select("""
        SELECT c.id,c.name,c.tier,c.province,
          (SELECT COUNT(*) FROM sales_order o WHERE o.customer_id=c.id AND o.status<>'CANCELLED') order_count,
          (SELECT MAX(o.ordered_at) FROM sales_order o WHERE o.customer_id=c.id) last_order_at,
          (SELECT COALESCE(SUM(p.amount),0) FROM payment p JOIN sales_order o ON o.id=p.order_id
            WHERE o.customer_id=c.id AND p.status='SUCCESS') paid_amount,
          (SELECT COUNT(*) FROM return_order r WHERE r.customer_id=c.id) return_count,
          (SELECT COUNT(*) FROM customer_coupon cc WHERE cc.customer_id=c.id AND cc.status='AVAILABLE') coupon_count
        FROM customer c WHERE c.tenant_id=#{tenantId} ORDER BY c.id
        """)
    List<Map<String, Object>> segments(@Param("tenantId") long tenantId);

    @Select("""
        SELECT o.id order_id,o.order_no,o.ordered_at,m.id movement_id,m.warehouse_id,s.code sku_code,
          -m.quantity quantity,m.occurred_at
        FROM sales_order o JOIN stock_movement m ON m.reference_no=o.order_no JOIN sku s ON s.id=m.sku_id
        WHERE o.customer_id=#{customerId}
        ORDER BY m.occurred_at DESC,m.id DESC
        """)
    List<Map<String, Object>> outboundRecords(@Param("customerId") long customerId);

    @Select("""
        SELECT DISTINCT o.id,o.order_no,o.customer_id,o.status,o.ordered_at,o.total_amount
        FROM sales_order o LEFT JOIN return_order r ON r.order_id=o.id
        WHERE o.customer_id=#{customerId} OR r.customer_id=#{customerId}
        ORDER BY o.ordered_at DESC,o.id DESC
        """)
    List<Map<String, Object>> relatedOrders(@Param("customerId") long customerId);

    @Select("""
        SELECT o.id,o.order_no,o.status,o.ordered_at,o.total_amount,
          (SELECT COUNT(*) FROM sales_order_item i WHERE i.order_id=o.id) line_count,
          (SELECT COALESCE(-SUM(m.quantity),0) FROM stock_movement m WHERE m.reference_no=o.order_no) shipped_units
        FROM sales_order o WHERE o.customer_id=#{customerId}
        ORDER BY o.ordered_at DESC,o.id DESC
        """)
    List<Map<String, Object>> orderFulfillment(@Param("customerId") long customerId);

    @Select("""
        SELECT i.sku_id,MAX(s.code) sku_code,MAX(p.name) product_name,COUNT(DISTINCT o.id) order_count,COUNT(*) purchase_times,
          SUM(i.quantity) units,MAX(i.created_at) last_bought_at
        FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id
        JOIN product p ON p.id=s.product_id
        GROUP BY o.customer_id,i.sku_id HAVING o.customer_id=#{customerId}
        ORDER BY units DESC,i.sku_id LIMIT 20
        """)
    List<Map<String, Object>> frequentSkus(@Param("customerId") long customerId);
}
