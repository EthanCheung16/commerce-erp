package com.example.commerce.report;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface XmlReportMapper {
    /** 1号商户非取消订单·年度品类销售汇总 */
    List<Map<String, Object>> annualCategorySales(ReportFilter filter);

    /** 移动端或购物车商品成交 */
    List<Map<String, Object>> cartConversionRevenue(ReportFilter filter);

    /** 非取消订单渠道品类购买件数分布 */
    List<Map<String, Object>> channelBasketBands(ReportFilter filter);

    /** 1号商户非取消订单·渠道品牌销售额 */
    List<Map<String, Object>> channelBrandRevenue(ReportFilter filter);

    /** 各渠道销售指标 */
    List<Map<String, Object>> channelKpis(ReportFilter filter);

    /** 渠道销售总额 */
    List<Map<String, Object>> channelSalesSummary(ReportFilter filter);

    /** 成交订单·客户订单售后次数 */
    List<Map<String, Object>> customerReturnCounts(ReportFilter filter);

    /** 1号商户·每日订单商品销售额 */
    List<Map<String, Object>> dailySales(ReportFilter filter);

    /** 未退回商品·每日销售件数 */
    List<Map<String, Object>> dailySalesVolume(ReportFilter filter);

    /** 移动端订单商品汇总 */
    List<Map<String, Object>> mobileOrderItems(ReportFilter filter);

    /** 1–4号商户·移动端订单平均单价 */
    List<Map<String, Object>> mobileOrderPrices(ReportFilter filter);

    /** 1号商户非取消订单·月度品类成交金额 */
    List<Map<String, Object>> monthlyCategorySales(ReportFilter filter);

    /** 非取消订单·月度销售额 */
    List<Map<String, Object>> monthlyRevenue(ReportFilter filter);

    /** 2号商户·已支付订单金额排名 */
    List<Map<String, Object>> paidOrderAmounts(ReportFilter filter);

    /** 已支付订单商品统计 */
    List<Map<String, Object>> paidOrderItems(ReportFilter filter);

    /** 平台渠道或持券客户销售 */
    List<Map<String, Object>> promotionAudienceRevenue(ReportFilter filter);

    /** 促销活动商品销量 */
    List<Map<String, Object>> promotionVolume(ReportFilter filter);

    /** 商户季度品类销售额 */
    List<Map<String, Object>> tenantQuarterRevenue(ReportFilter filter);

    /** 1–4号商户·网页订单商品数量排名 */
    List<Map<String, Object>> webOrderQuantities(ReportFilter filter);

    /** 已发货及已完成订单客户商品行数 */
    List<Map<String, Object>> customerOrderLineCounts(ReportFilter filter);

    /** 非取消订单·客户消费排名 */
    List<Map<String, Object>> customerSpending(ReportFilter filter);

    /** 1号商户非取消订单·客户邮箱域名销售贡献 */
    List<Map<String, Object>> emailDomainSales(ReportFilter filter);

    /** 金卡客户累计消费 */
    List<Map<String, Object>> goldCustomerLifetimeValue(ReportFilter filter);

    /** 1–4号商户·金卡客户订单金额排名 */
    List<Map<String, Object>> goldCustomerOrders(ReportFilter filter);

    /** 1–4号商户·广东客户订单件数排名 */
    List<Map<String, Object>> guangdongOrders(ReportFilter filter);

    /** 高等级或高消费客户销售 */
    List<Map<String, Object>> priorityCustomerRevenue(ReportFilter filter);

    /** 1号商户非取消订单·省份品类销售汇总 */
    List<Map<String, Object>> provinceCategorySales(ReportFilter filter);

    /** 广东客户累计成交 */
    List<Map<String, Object>> provinceCustomerValue(ReportFilter filter);

    /** 近期注册客户购买品种 */
    List<Map<String, Object>> recentCustomerPurchaseCount(ReportFilter filter);

    /** 会员等级可用优惠券触达 */
    List<Map<String, Object>> tierAvailableCouponCounts(ReportFilter filter);

    /** 品牌渠道金额 */
    List<Map<String, Object>> brandChannelRevenue(ReportFilter filter);

    /** 已发货及退回商品优惠覆盖指标 */
    List<Map<String, Object>> categoryDiscountCoverage(ReportFilter filter);

    /** 品类 SKU 销售额 */
    List<Map<String, Object>> categorySkuRevenue(ReportFilter filter);

    /** 品类按订单状态销售 */
    List<Map<String, Object>> categoryStatusRevenue(ReportFilter filter);

    /** 渠道商品评价覆盖 */
    List<Map<String, Object>> channelReviewedLineCounts(ReportFilter filter);

    /** 高价 SKU 购买人数 */
    List<Map<String, Object>> highPriceSkuBuyers(ReportFilter filter);

    /** 下架商品历史销量 */
    List<Map<String, Object>> offlineProductSales(ReportFilter filter);

    /** 1号商户非取消订单·价格带销售贡献 */
    List<Map<String, Object>> priceBandSales(ReportFilter filter);

    /** 1–3号商户·退回或低评分商品销售 */
    List<Map<String, Object>> qualityConcernRevenue(ReportFilter filter);

    /** 未退回商品·SKU 销售额排名 */
    List<Map<String, Object>> skuRevenueRanking(ReportFilter filter);

    /** 1号商户非取消订单·商品销售排行榜 */
    List<Map<String, Object>> skuSalesRanking(ReportFilter filter);

    /** 退款中或已申请售后品类 */
    List<Map<String, Object>> afterSaleCategoryRevenue(ReportFilter filter);

    /** 非取消订单·品类销售时库存点数量 */
    List<Map<String, Object>> categoryWarehouseAvailability(ReportFilter filter);

    /** 1–4号商户·已完成订单重量排名 */
    List<Map<String, Object>> completedOrderWeight(ReportFilter filter);

    /** 1–4号商户·包邮订单货值排名 */
    List<Map<String, Object>> freeShippingOrders(ReportFilter filter);

    /** 未发货或运输中订单货值 */
    List<Map<String, Object>> fulfillmentRiskRevenue(ReportFilter filter);

    /** 未退回商品·履约状态明细数 */
    List<Map<String, Object>> fulfillmentSummary(ReportFilter filter);

    /** 大额或多轨迹订单货值 */
    List<Map<String, Object>> logisticsConcernRevenue(ReportFilter filter);

    /** 畅销或低库存品牌销售 */
    List<Map<String, Object>> lowStockBrandRevenue(ReportFilter filter);

    /** 商户退货明细覆盖 */
    List<Map<String, Object>> tenantReturnedLineCounts(ReportFilter filter);

    /** 仓库履约阶段件数 */
    List<Map<String, Object>> warehouseFulfillmentUnits(ReportFilter filter);

    /** 仓库品类会员等级异常订单金额 */
    List<Map<String, Object>> warehouseOrderStateValue(ReportFilter filter);

    /** 1–4号商户·一号仓订单货值排名 */
    List<Map<String, Object>> warehouseOrderValue(ReportFilter filter);

    /** 未退回商品·仓库销售件数 */
    List<Map<String, Object>> warehouseSalesVolume(ReportFilter filter);

    /** 成交订单·仓库订单发运次数 */
    List<Map<String, Object>> warehouseShipmentCounts(ReportFilter filter);

    /** 1–2号商户非取消订单·工作日履约分布 */
    List<Map<String, Object>> weekdayFulfillment(ReportFilter filter);

    /** 品牌商品采购批次覆盖 */
    List<Map<String, Object>> brandPurchaseCoverage(ReportFilter filter);

    /** 高数量或待采购供应商销售 */
    List<Map<String, Object>> replenishmentSupplierRevenue(ReportFilter filter);

    /** 供应商商品五星评价覆盖 */
    List<Map<String, Object>> supplierPositiveReviewCoverage(ReportFilter filter);

    /** 供应商价格带销售 */
    List<Map<String, Object>> supplierPriceBands(ReportFilter filter);

    /** 供应商 SKU 需求 */
    List<Map<String, Object>> supplierSkuDemand(ReportFilter filter);

    /** 1–4号商户·年度订单明细优惠排名 */
    List<Map<String, Object>> annualOrderDiscounts(ReportFilter filter);

    /** 未退回商品品牌收入成本毛利 */
    List<Map<String, Object>> brandMarginMetrics(ReportFilter filter);

    /** 品牌 SKU 优惠额 */
    List<Map<String, Object>> brandSkuDiscount(ReportFilter filter);

    /** 成交订单·渠道支付记录覆盖情况 */
    List<Map<String, Object>> channelPaidOrderCounts(ReportFilter filter);

    /** 1号商户·订单账龄商品金额 */
    List<Map<String, Object>> orderAgeSales(ReportFilter filter);

    /** 未退回商品·订单状态货值 */
    List<Map<String, Object>> orderStatusValue(ReportFilter filter);

    /** 未付款或退款订单货值 */
    List<Map<String, Object>> paymentExceptionRevenue(ReportFilter filter);

    /** 非取消订单·商户明细优惠总额 */
    List<Map<String, Object>> tenantDiscountSummary(ReportFilter filter);

    /** 1–4号商户·商户订单毛利排名 */
    List<Map<String, Object>> tenantOrderMargin(ReportFilter filter);

}
