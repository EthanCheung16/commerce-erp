package com.example.commerce.aftersale;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AfterSaleMapper {
    @Select("""
        <script>
        SELECT r.id,r.return_no,r.order_id,r.customer_id,r.reason,r.status,r.refund_amount,r.requested_at,c.name customer_name
        FROM return_order r JOIN customer c ON c.id=r.customer_id
        <where>
          <if test="status != null">r.status=#{status}</if>
          <if test="beforeId != null">AND r.id &lt; #{beforeId}</if>
        </where>
        ORDER BY r.id DESC LIMIT #{limit}
        </script>
        """)
    List<Map<String, Object>> returns(@Param("status") String status, @Param("beforeId") Long beforeId,
            @Param("limit") int limit);

    @Select("""
        SELECT r.*,o.order_no,c.name customer_name FROM return_order r
        JOIN sales_order o ON o.id=r.order_id JOIN customer c ON c.id=r.customer_id WHERE r.id=#{id}
        """)
    Map<String, Object> returnOrder(long id);

    @Select("""
        SELECT ri.*,i.sku_id,s.code sku_code FROM return_item ri JOIN sales_order_item i ON i.id=ri.order_item_id
        JOIN sku s ON s.id=i.sku_id WHERE ri.return_order_id=#{id} ORDER BY ri.id
        """)
    List<Map<String, Object>> returnItems(long id);

    @Select("""
        SELECT r.id,r.return_no,r.order_id,o.order_no,r.status,r.reason,r.requested_at,r.refund_amount
        FROM return_order r JOIN sales_order o ON o.id=r.order_id WHERE r.return_no=#{returnNo}
        """)
    List<Map<String, Object>> returnByNo(@Param("returnNo") String returnNo);

    @Select("""
        SELECT r.id,r.return_no,r.order_id,r.status,r.requested_at,r.refund_amount,
          (SELECT SUM(ri.quantity) FROM return_item ri WHERE ri.return_order_id=r.id) return_qty,
          (SELECT COALESCE(SUM(m.quantity),0) FROM stock_movement m WHERE m.reference_no=r.return_no) restocked_qty
        FROM return_order r
        WHERE r.requested_at>=#{from} AND r.requested_at<#{until}
        ORDER BY r.requested_at,r.id
        """)
    List<Map<String, Object>> restockCheck(@Param("from") LocalDate from, @Param("until") LocalDate until);

    @Select("""
        SELECT c.id customer_id,c.name customer_name,c.tier,x.paid_amount,x.refunded_payment,y.refund_amount,
          COALESCE(x.refunded_payment,0)-COALESCE(y.refund_amount,0) difference
        FROM customer c
        JOIN (SELECT o.customer_id,SUM(CASE WHEN p.status='SUCCESS' THEN p.amount ELSE 0 END) paid_amount,
                SUM(CASE WHEN p.status='REFUNDED' THEN p.amount ELSE 0 END) refunded_payment
              FROM payment p JOIN sales_order o ON o.id=p.order_id GROUP BY o.customer_id) x ON x.customer_id=c.id
        LEFT JOIN (SELECT customer_id,SUM(refund_amount) refund_amount FROM return_order
                   WHERE status='REFUNDED' GROUP BY customer_id) y ON y.customer_id=c.id
        WHERE c.tenant_id=#{tenantId} AND x.refunded_payment>0
        ORDER BY difference DESC,c.id LIMIT 100
        """)
    List<Map<String, Object>> refundCheck(@Param("tenantId") long tenantId);

    @Select("""
        SELECT r.id return_id,r.return_no,r.order_id,r.status,r.requested_at,m.id movement_id,m.warehouse_id,
          m.sku_id,m.quantity,m.occurred_at
        FROM return_order r JOIN stock_movement m ON m.reference_no=r.return_no
        WHERE r.requested_at>=#{from} AND r.requested_at<#{until}
        ORDER BY r.requested_at,r.id,m.id
        """)
    List<Map<String, Object>> returnMovements(@Param("from") LocalDate from, @Param("until") LocalDate until);
}
