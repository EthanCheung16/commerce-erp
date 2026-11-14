package com.example.commerce.finance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FinanceMapper {
    @Select("""
        SELECT p.id,p.transaction_no,p.method,p.status,p.amount,p.paid_at,o.order_no,o.customer_id,o.total_amount
        FROM payment p JOIN sales_order o ON o.id=p.order_id
        WHERE p.transaction_no=#{transactionNo}
        """)
    List<Map<String, Object>> paymentByTransactionNo(@Param("transactionNo") String transactionNo);

    @Select("""
        SELECT p.id,p.transaction_no,p.method,p.status,p.amount,p.paid_at,o.order_no,o.customer_id
        FROM payment p JOIN sales_order o ON o.id=p.order_id
        WHERE DATE(p.paid_at)=#{day}
        ORDER BY p.paid_at,p.id
        """)
    List<Map<String, Object>> dailyPayments(@Param("day") LocalDate day);

    @Select("""
        SELECT o.id,o.order_no,o.customer_id,o.paid_at,o.total_amount,p.transaction_no,p.method,p.amount,sh.tracking_no,sh.shipped_at
        FROM sales_order o JOIN payment p ON p.order_id=o.id LEFT JOIN shipment sh ON sh.order_id=o.id
        WHERE DATE(o.paid_at)=#{day} OR DATE(p.paid_at)=#{day} OR DATE(sh.shipped_at)=#{day}
        ORDER BY o.paid_at,o.id,p.id
        """)
    List<Map<String, Object>> ordersByPaidDate(@Param("day") LocalDate day);

    @Select("""
        SELECT p.id,p.transaction_no,p.method,p.status,p.amount,p.paid_at,o.order_no,o.customer_id
        FROM payment p JOIN sales_order o ON o.id=p.order_id
        ORDER BY p.paid_at DESC,p.id DESC LIMIT #{offset},#{size}
        """)
    List<Map<String, Object>> paymentPage(@Param("offset") int offset, @Param("size") int size);

    @Select("""
        SELECT p.id,p.transaction_no,p.method,p.status,p.amount,p.paid_at,o.order_no,o.customer_id,c.phone
        FROM payment p JOIN sales_order o ON o.id=p.order_id JOIN customer c ON c.id=o.customer_id
        WHERE p.transaction_no LIKE CONCAT('%',#{keyword},'%') OR o.order_no LIKE CONCAT('%',#{keyword},'%')
           OR c.phone LIKE CONCAT('%',#{keyword},'%')
        ORDER BY p.paid_at DESC,p.id DESC LIMIT 20
        """)
    List<Map<String, Object>> paymentSearch(@Param("keyword") String keyword);

    @Select("""
        SELECT o.id,o.order_no,o.status,o.ordered_at,o.total_amount,x.paid_amount,
          o.total_amount-COALESCE(x.paid_amount,0) difference
        FROM sales_order o
        LEFT JOIN (SELECT order_id,SUM(amount) paid_amount FROM payment WHERE status='SUCCESS' GROUP BY order_id) x
          ON x.order_id=o.id
        WHERE o.status IN ('PAID','SHIPPED','COMPLETED') AND o.ordered_at>=#{from} AND o.ordered_at<#{until}
          AND (x.paid_amount IS NULL OR x.paid_amount<>o.total_amount)
        ORDER BY o.ordered_at,o.id LIMIT 200
        """)
    List<Map<String, Object>> paymentCheck(@Param("from") LocalDate from, @Param("until") LocalDate until);

    @Select("""
        WITH daily_receipts AS (
            SELECT p.method,DATE(p.paid_at) paid_day,SUM(p.amount) amount
            FROM payment p LEFT JOIN sales_order o ON o.id=p.order_id LEFT JOIN customer c ON c.id=o.customer_id
            WHERE p.status='SUCCESS'
            GROUP BY p.method,DATE(p.paid_at)
            HAVING MIN(p.paid_at)>=#{from}
        ), period_receipts AS (
            SELECT p.id,p.method,p.transaction_no,p.paid_at,p.amount,
              SUM(p.amount) OVER (PARTITION BY p.method,DATE(p.paid_at)
                ORDER BY p.paid_at RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) day_amount
            FROM payment p
            WHERE p.status='SUCCESS' AND p.paid_at>=#{from} AND p.paid_at<#{until}
        )
        SELECT p.id,p.transaction_no,p.paid_at,p.amount,
          p.day_amount+COALESCE((SELECT SUM(CASE WHEN d.paid_day<DATE(p.paid_at) THEN d.amount ELSE 0 END)
            FROM daily_receipts d WHERE d.method=p.method
              AND d.paid_day<=DATE(p.paid_at)),0) running_amount
        FROM period_receipts p WHERE p.method=#{method}
        ORDER BY p.paid_at,p.id
        """)
    List<Map<String, Object>> paymentLedger(@Param("method") String method, @Param("from") LocalDate from,
            @Param("until") LocalDate until);

    @Select("""
        WITH RECURSIVE days(d) AS (
          SELECT DATE(#{from}) UNION ALL SELECT d+INTERVAL 1 DAY FROM days WHERE d+INTERVAL 1 DAY<DATE(#{until}))
        SELECT d.d day,
          (SELECT COALESCE(SUM(p.amount),0) FROM payment p WHERE p.status='SUCCESS' AND DATE(p.paid_at)=d.d) received_amount,
          (SELECT COALESCE(SUM(p.amount),0) FROM payment p WHERE p.status='REFUNDED' AND DATE(p.paid_at)=d.d) refunded_amount,
          (SELECT COALESCE(SUM(o.total_amount),0) FROM sales_order o WHERE DATE(o.paid_at)=d.d) order_paid_amount
        FROM days d ORDER BY d.d
        """)
    List<Map<String, Object>> calendar(@Param("from") LocalDate from, @Param("until") LocalDate until);

    @Select("""
        SELECT st.id,st.batch_no,st.platform_order_no,st.settled_amount,st.commission,st.settled_at,
          o.order_no,o.status,o.total_amount
        FROM marketplace_settlement st LEFT JOIN sales_order o ON o.platform_order_no=st.platform_order_no
        WHERE st.settled_at>=#{from} AND st.settled_at<#{until}
        ORDER BY st.settled_at,st.id
        """)
    List<Map<String, Object>> settlements(@Param("from") LocalDate from, @Param("until") LocalDate until);

    @Select("""
        SELECT st.batch_no,COUNT(*) settlement_count,SUM(st.settled_amount) settled_amount,SUM(st.commission) commission,
          SUM(o.id IS NULL) unmatched_count,SUM(li.units) units
        FROM marketplace_settlement st LEFT JOIN sales_order o ON o.platform_order_no=st.platform_order_no
        LEFT JOIN (SELECT i.order_id,SUM(i.quantity) units FROM sales_order_item i JOIN sales_order so ON so.id=i.order_id
                   WHERE so.channel='MARKETPLACE' GROUP BY i.order_id) li ON li.order_id=o.id
        WHERE st.batch_no BETWEEN #{fromBatch} AND #{toBatch}
        GROUP BY st.batch_no ORDER BY st.batch_no
        """)
    List<Map<String, Object>> settlementSummary(@Param("fromBatch") String fromBatch, @Param("toBatch") String toBatch);

    @Select("""
        SELECT st.id,st.batch_no,st.platform_order_no,st.settled_amount,st.commission,st.settled_at,
          (SELECT o.order_no FROM sales_order o WHERE o.platform_order_no=st.platform_order_no) order_no,
          (SELECT o.status FROM sales_order o WHERE o.platform_order_no=st.platform_order_no) order_status,
          (SELECT o.total_amount FROM sales_order o WHERE o.platform_order_no=st.platform_order_no) order_amount
        FROM marketplace_settlement st WHERE st.batch_no=#{batchNo}
        ORDER BY st.id
        """)
    List<Map<String, Object>> settlementDetail(@Param("batchNo") String batchNo);

    @Select("""
        WITH platform_orders AS (
            SELECT o.id,o.platform_order_no,o.order_no,SUM(o.total_amount) total_amount
            FROM sales_order o LEFT JOIN customer c ON c.id=o.customer_id
            WHERE o.platform_order_no IS NOT NULL
            GROUP BY o.id,o.platform_order_no,o.order_no
        ), settlement_orders AS (
            SELECT st.id settlement_id,o.id order_id,o.order_no,o.total_amount
            FROM marketplace_settlement st JOIN platform_orders o ON o.platform_order_no=st.platform_order_no
            WHERE st.batch_no BETWEEN #{fromBatch} AND #{toBatch}
            GROUP BY st.id,o.id,o.order_no,o.total_amount
        )
        SELECT st.id,st.batch_no,st.platform_order_no,st.settled_amount,st.commission,st.settled_at,
          (SELECT o.order_no FROM settlement_orders o WHERE o.settlement_id=st.id) order_no,
          (SELECT o.total_amount FROM settlement_orders o WHERE o.settlement_id=st.id) order_amount
        FROM marketplace_settlement st
        WHERE st.batch_no BETWEEN #{fromBatch} AND #{toBatch}
          AND st.settled_amount<>(SELECT o.total_amount FROM settlement_orders o WHERE o.settlement_id=st.id)
        ORDER BY st.batch_no,st.id
        """)
    List<Map<String, Object>> settlementDifferences(@Param("fromBatch") String fromBatch,
            @Param("toBatch") String toBatch);
}
