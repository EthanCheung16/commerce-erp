package com.example.commerce.report;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnnotationReportMapper {
    /** 渠道商品平均售价 */
    @Select("""
SELECT o.channel dimension,AVG(i.unit_price) average_price FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
LEFT JOIN warehouse w ON w.id=o.warehouse_id
LEFT JOIN tenant t ON t.id=o.tenant_id
LEFT JOIN sku s ON s.id=i.sku_id
LEFT JOIN product p ON p.id=s.product_id
LEFT JOIN category ca ON ca.id=p.category_id
LEFT JOIN brand b ON b.id=p.brand_id
LEFT JOIN supplier su ON su.id=p.supplier_id
WHERE i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY o.channel ORDER BY average_price DESC LIMIT 50
        """)
    List<Map<String, Object>> channelAveragePrice(ReportFilter filter);

    /** 渠道工作日周末销售 */
    @Select("""
SELECT o.channel dimension,'weekday' metric,SUM(i.line_amount) value FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id WHERE (DAYOFWEEK(i.created_at) BETWEEN 2 AND 6) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY o.channel
UNION
SELECT o.channel dimension,'weekend' metric,SUM(i.line_amount) value FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id WHERE (DAYOFWEEK(i.created_at) IN (1,7)) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY o.channel
UNION
SELECT o.channel dimension,'all_days' metric,SUM(i.line_amount) value FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id WHERE (1=1) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY o.channel
ORDER BY value DESC LIMIT 50
        """)
    List<Map<String, Object>> channelCalendarMix(ReportFilter filter);

    /** 未退回商品·渠道购物车关注度 */
    @Select("""
SELECT o.channel dimension,SUM((SELECT COUNT(*) FROM shopping_cart_item x WHERE x.sku_id=i.sku_id)) cart_touches,SUM(i.line_amount) revenue
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN customer c ON c.id=o.customer_id
WHERE i.fulfillment_status<>'RETURNED' AND i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY o.channel ORDER BY cart_touches DESC LIMIT 50
        """)
    List<Map<String, Object>> channelCartInterest(ReportFilter filter);

    /** 未退回商品·渠道品类会员等级销售额 */
    @Select("""
SELECT o.channel,p.category_id,c.tier,SUM(i.line_amount) revenue,COUNT(*) line_count
FROM sales_order_item i JOIN sales_order o ON CAST(o.id AS CHAR)=CAST(i.order_id AS CHAR)
JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id JOIN customer c ON c.id=o.customer_id
WHERE i.fulfillment_status<>'RETURNED' AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY o.channel,p.category_id,c.tier ORDER BY revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> channelCategorySales(ReportFilter filter);

    /** 1–4号商户·享受订单优惠的商品数量 */
    @Select("""
SELECT o.id order_id,MAX(o.order_no) order_no,SUM(i.quantity) units
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
WHERE o.tenant_id IN (1,2,3,4)
GROUP BY o.id HAVING MAX(o.discount_amount)>0 ORDER BY units DESC,o.id LIMIT 50
        """)
    List<Map<String, Object>> discountedOrders(ReportFilter filter);

    /** 1号商户非取消订单·促销活动销售统计 */
    @Select("""
SELECT COALESCE(i.promotion_code,'NONE') promotion,p.category_id,SUM(i.line_amount) revenue
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id WHERE o.status<>'CANCELLED' AND o.tenant_id=1 AND COALESCE(i.promotion_code,'NONE') <> 'INVALID' GROUP BY promotion,p.category_id ORDER BY revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> promotionSales(ReportFilter filter);

    /** 商户成交订单数 */
    @Select("""
SELECT o.tenant_id dimension,COUNT(DISTINCT o.id) order_count FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
LEFT JOIN customer c ON c.id=o.customer_id
LEFT JOIN customer_address a ON a.id=o.address_id
LEFT JOIN warehouse w ON w.id=o.warehouse_id
LEFT JOIN tenant t ON t.id=o.tenant_id
LEFT JOIN sku s ON s.id=i.sku_id
LEFT JOIN product p ON p.id=s.product_id
LEFT JOIN category ca ON ca.id=p.category_id
LEFT JOIN brand b ON b.id=p.brand_id
LEFT JOIN supplier su ON su.id=p.supplier_id
WHERE i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY o.tenant_id ORDER BY order_count DESC LIMIT 50
        """)
    List<Map<String, Object>> tenantOrderCoverage(ReportFilter filter);

    /** 1号商户非取消订单·手机号段客户消费贡献 */
    @Select("""
SELECT LEFT(c.phone,3) prefix,c.tier,p.category_id,SUM(i.line_amount) revenue
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id JOIN customer c ON c.id=o.customer_id WHERE o.status<>'CANCELLED' AND o.tenant_id=1 AND LEFT(c.phone,3)='138' GROUP BY prefix,c.tier,p.category_id ORDER BY revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> phonePrefixSales(ReportFilter filter);

    /** 1–4号商户·白金客户订单优惠额 */
    @Select("""
SELECT o.id order_id,MAX(o.order_no) order_no,SUM(i.discount_amount) discount_total
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN customer c ON c.id=o.customer_id
WHERE o.tenant_id IN (1,2,3,4)
GROUP BY o.id HAVING MAX(c.tier)='PLATINUM' ORDER BY discount_total DESC,o.id LIMIT 50
        """)
    List<Map<String, Object>> platinumOrderDiscounts(ReportFilter filter);

    /** 省份客户多地址覆盖 */
    @Select("""
SELECT c.province dimension,SUM((SELECT COUNT(*) FROM customer_address x WHERE x.customer_id=o.customer_id)) address_touches,SUM(i.line_amount) revenue
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN customer c ON c.id=o.customer_id
WHERE i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY c.province ORDER BY address_touches DESC LIMIT 50
        """)
    List<Map<String, Object>> provinceCustomerAddressCounts(ReportFilter filter);

    /** 白金或多次购买客户销售 */
    @Select("""
SELECT c.province dimension,SUM(i.line_amount) revenue,SUM(i.quantity) units,COUNT(*) line_count
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id JOIN customer c ON c.id=o.customer_id
WHERE (c.tier='PLATINUM' OR o.customer_id IN (SELECT x.customer_id FROM sales_order x JOIN sales_order_item xi ON xi.order_id=x.id GROUP BY x.customer_id HAVING COUNT(DISTINCT xi.sku_id)>10)) AND i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY c.province ORDER BY revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> repeatBuyerRevenue(ReportFilter filter);

    /** 商户客户购买品牌数 */
    @Select("""
SELECT c.id entity_id,x.* FROM customer c
JOIN (SELECT o.customer_id,COUNT(DISTINCT pp.brand_id) brand_count,SUM(i.line_amount) revenue FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku ss ON ss.id=i.sku_id JOIN product pp ON pp.id=ss.product_id
WHERE i.created_at>=#{from} AND i.created_at<#{until} GROUP BY o.customer_id) x ON c.id=x.customer_id
WHERE c.tenant_id=3 ORDER BY x.brand_count DESC LIMIT 50
        """)
    List<Map<String, Object>> tenantCustomerBreadth(ReportFilter filter);

    /** 成交品类 SKU 丰富度 */
    @Select("""
SELECT p.category_id dimension,SUM((SELECT COUNT(*) FROM sku x WHERE x.product_id=p.id)) sku_touches,SUM(i.line_amount) revenue
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id JOIN customer c ON c.id=o.customer_id
WHERE i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY p.category_id ORDER BY sku_touches DESC LIMIT 50
        """)
    List<Map<String, Object>> categorySkuBreadth(ReportFilter filter);

    /** 大件 SKU 成交金额 */
    @Select("""
SELECT s.id entity_id,x.* FROM sku s
JOIN (SELECT i.sku_id,SUM(i.line_amount) revenue,SUM(i.quantity) units FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku ss ON ss.id=i.sku_id JOIN product pp ON pp.id=ss.product_id
WHERE i.created_at>=#{from} AND i.created_at<#{until} GROUP BY i.sku_id) x ON s.id=x.sku_id
WHERE s.weight_grams>4000 ORDER BY x.revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> heavySkuRevenue(ReportFilter filter);

    /** 品类重量段成交 */
    @Select("""
SELECT p.category_id dimension,'light' metric,SUM(i.quantity) value FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id WHERE (s.weight_grams<1000) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY p.category_id
UNION
SELECT p.category_id dimension,'medium' metric,SUM(i.quantity) value FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id WHERE (s.weight_grams>=1000 AND s.weight_grams<3000) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY p.category_id
UNION
SELECT p.category_id dimension,'heavy' metric,SUM(i.quantity) value FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id WHERE (s.weight_grams>=3000) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY p.category_id
ORDER BY value DESC LIMIT 50
        """)
    List<Map<String, Object>> categoryWeightBands(ReportFilter filter);

    /** 成交订单·省份门店及高周转商品销售 */
    @Select("""
SELECT p.category_id dimension,a.province,SUM(i.line_amount) revenue,SUM(i.quantity) units,COUNT(*) line_count
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id JOIN customer_address a ON a.id=o.address_id
WHERE o.status IN ('PAID','SHIPPED','COMPLETED','REFUNDING') AND (o.channel='STORE' OR i.sku_id IN (SELECT sku_id FROM stock_movement GROUP BY sku_id HAVING SUM(ABS(quantity))>100)) AND i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY p.category_id,a.province ORDER BY revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> inventoryMovementRevenue(ReportFilter filter);

    /** 1–3号商户·折扣或高退货商品销售 */
    @Select("""
SELECT p.brand_id dimension,SUM(i.line_amount) revenue,SUM(i.quantity) units,COUNT(*) line_count
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id
WHERE o.tenant_id BETWEEN 1 AND 3 AND (i.discount_amount>=7 OR i.sku_id IN (SELECT xi.sku_id FROM return_item ri JOIN sales_order_item xi ON xi.id=ri.order_item_id GROUP BY xi.sku_id HAVING COUNT(*)>=5)) AND i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY p.brand_id ORDER BY revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> returnExposureRevenue(ReportFilter filter);

    /** 1–4号商户·已发货订单商品种类 */
    @Select("""
SELECT o.id order_id,MAX(o.order_no) order_no,COUNT(DISTINCT i.sku_id) sku_count
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
WHERE o.tenant_id IN (1,2,3,4)
GROUP BY o.id HAVING MAX(o.status)='SHIPPED' ORDER BY sku_count DESC,o.id LIMIT 50
        """)
    List<Map<String, Object>> shippedOrderLines(ReportFilter filter);

    /** 1–2号商户非取消订单·仓库每日销量 */
    @Select("""
SELECT CAST(i.created_at AS DATE) sale_date,o.warehouse_id,SUM(i.quantity) units
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id WHERE o.status<>'CANCELLED' AND o.tenant_id IN (1,2) AND CAST(i.created_at AS DATE)>=CAST(#{from} AS DATE) AND CAST(i.created_at AS DATE)<CAST(#{until} AS DATE)
GROUP BY sale_date,o.warehouse_id ORDER BY units DESC LIMIT 50
        """)
    List<Map<String, Object>> warehouseDailySales(ReportFilter filter);

    /** 仓库订单重量 */
    @Select("""
SELECT selected.id entity_id,x.* FROM sales_order selected
JOIN (SELECT i.order_id,SUM(i.quantity*ss.weight_grams) weight_grams,SUM(i.line_amount) revenue FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku ss ON ss.id=i.sku_id JOIN product pp ON pp.id=ss.product_id
WHERE i.created_at>=#{from} AND i.created_at<#{until} GROUP BY i.order_id) x ON selected.id=x.order_id
WHERE selected.warehouse_id=2 ORDER BY x.weight_grams DESC LIMIT 50
        """)
    List<Map<String, Object>> warehouseOrderWeight(ReportFilter filter);

    /** 仓库成交 SKU 数 */
    @Select("""
SELECT o.warehouse_id dimension,COUNT(DISTINCT i.sku_id) sku_count FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
LEFT JOIN warehouse w ON w.id=o.warehouse_id
LEFT JOIN tenant t ON t.id=o.tenant_id
LEFT JOIN sku s ON s.id=i.sku_id
LEFT JOIN product p ON p.id=s.product_id
LEFT JOIN category ca ON ca.id=p.category_id
LEFT JOIN brand b ON b.id=p.brand_id
LEFT JOIN supplier su ON su.id=p.supplier_id
WHERE i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY o.warehouse_id ORDER BY sku_count DESC LIMIT 50
        """)
    List<Map<String, Object>> warehouseProductCoverage(ReportFilter filter);

    /** 非取消订单·仓库物流轨迹覆盖 */
    @Select("""
SELECT o.warehouse_id dimension,SUM((SELECT COUNT(*) FROM shipment x JOIN shipment_event e ON e.shipment_id=x.id WHERE x.order_id=o.id)) event_touches,SUM(i.line_amount) revenue
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id
WHERE o.status<>'CANCELLED' AND i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY o.warehouse_id ORDER BY event_touches DESC LIMIT 50
        """)
    List<Map<String, Object>> warehouseShipmentEventCounts(ReportFilter filter);

    /** 高价或高采购量商品销售 */
    @Select("""
SELECT p.supplier_id dimension,SUM(i.line_amount) revenue,SUM(i.quantity) units,COUNT(*) line_count
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id JOIN customer c ON c.id=o.customer_id
WHERE (i.unit_price>700 OR i.sku_id IN (SELECT sku_id FROM purchase_order_item GROUP BY sku_id HAVING SUM(quantity)>200)) AND i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY p.supplier_id ORDER BY revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> supplierConcentrationRevenue(ReportFilter filter);

    /** 供应商促销参与结构 */
    @Select("""
SELECT p.supplier_id dimension,'promoted' metric,SUM(i.line_amount) value FROM sales_order_item i JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id WHERE (i.promotion_code IS NOT NULL) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY p.supplier_id
UNION
SELECT p.supplier_id dimension,'organic' metric,SUM(i.line_amount) value FROM sales_order_item i JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id WHERE (i.promotion_code IS NULL) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY p.supplier_id
UNION
SELECT p.supplier_id dimension,'total' metric,SUM(i.line_amount) value FROM sales_order_item i JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id WHERE (1=1) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY p.supplier_id
ORDER BY value DESC LIMIT 50
        """)
    List<Map<String, Object>> supplierPromotionMix(ReportFilter filter);

    /** 渠道商品优惠率 */
    @Select("""
SELECT o.channel dimension,SUM(i.discount_amount)/NULLIF(SUM(i.unit_price*i.quantity),0) discount_rate FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
LEFT JOIN customer c ON c.id=o.customer_id
LEFT JOIN warehouse w ON w.id=o.warehouse_id
LEFT JOIN tenant t ON t.id=o.tenant_id
LEFT JOIN sku s ON s.id=i.sku_id
LEFT JOIN product p ON p.id=s.product_id
LEFT JOIN category ca ON ca.id=p.category_id
LEFT JOIN brand b ON b.id=p.brand_id
LEFT JOIN supplier su ON su.id=p.supplier_id
WHERE i.created_at>=#{from} AND i.created_at<#{until}
GROUP BY o.channel ORDER BY discount_rate DESC LIMIT 50
        """)
    List<Map<String, Object>> channelDiscountRate(ReportFilter filter);

    /** 1号商户非取消订单·折扣区间销售额 */
    @Select("""
SELECT ROUND(ABS(i.discount_amount),0) discount_band,p.brand_id,SUM(i.line_amount) revenue
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id JOIN product p ON p.id=s.product_id WHERE o.status<>'CANCELLED' AND o.tenant_id=1 AND ABS(i.discount_amount)>=0 GROUP BY discount_band,p.brand_id ORDER BY revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> discountBandSales(ReportFilter filter);

    /** 大额优惠订单明细总额 */
    @Select("""
SELECT selected.id entity_id,x.* FROM sales_order selected
JOIN (SELECT i.order_id,SUM(i.line_amount) revenue,SUM(i.discount_amount) discounts FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku ss ON ss.id=i.sku_id JOIN product pp ON pp.id=ss.product_id
WHERE i.created_at>=#{from} AND i.created_at<#{until} GROUP BY i.order_id) x ON selected.id=x.order_id
WHERE selected.discount_amount>=40 ORDER BY x.revenue DESC LIMIT 50
        """)
    List<Map<String, Object>> discountOrderValue(ReportFilter filter);

    /** 1–4号商户·平台渠道订单毛利 */
    @Select("""
SELECT o.id order_id,MAX(o.order_no) order_no,SUM(i.line_amount-s.cost*i.quantity) margin
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id JOIN sku s ON s.id=i.sku_id
WHERE o.tenant_id IN (1,2,3,4)
GROUP BY o.id HAVING MAX(o.channel)='MARKETPLACE' ORDER BY margin DESC,o.id LIMIT 50
        """)
    List<Map<String, Object>> marketplaceOrderMargin(ReportFilter filter);

    /** 1–4号商户·按支付时间筛选订单货值 */
    @Select("""
SELECT o.id order_id,MAX(o.order_no) order_no,SUM(i.line_amount) revenue
FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id
WHERE o.tenant_id IN (1,2,3,4)
GROUP BY o.id HAVING MIN(o.paid_at)>=#{from} AND MAX(o.paid_at)<#{until} ORDER BY revenue DESC,o.id LIMIT 50
        """)
    List<Map<String, Object>> paidDateOrderValue(ReportFilter filter);

    /** 商户运费与优惠分组 */
    @Select("""
SELECT o.tenant_id dimension,'free_shipping' metric,SUM(i.line_amount) value FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id WHERE (o.shipping_fee=0) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY o.tenant_id
UNION
SELECT o.tenant_id dimension,'paid_shipping' metric,SUM(i.line_amount) value FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id WHERE (o.shipping_fee>0) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY o.tenant_id
UNION
SELECT o.tenant_id dimension,'order_discount' metric,SUM(i.line_amount) value FROM sales_order_item i JOIN sales_order o ON o.id=i.order_id WHERE (o.discount_amount>0) AND i.created_at>=#{from} AND i.created_at<#{until} GROUP BY o.tenant_id
ORDER BY value DESC LIMIT 50
        """)
    List<Map<String, Object>> tenantShippingMix(ReportFilter filter);

}
